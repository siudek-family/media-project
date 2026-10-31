"""Confined filesystem operations for configured media roots."""

from __future__ import annotations

import hashlib
import json
import os
import stat
from datetime import datetime, timezone
from pathlib import Path, PureWindowsPath
from typing import Any, Iterator


class MediaAccessError(Exception):
    """Base error for rejected or failed media operations."""


class InvalidMediaPath(MediaAccessError):
    """Raised when a path is outside the configured filesystem boundary."""


class MediaConfigurationError(MediaAccessError):
    """Raised when a media root has not been configured correctly."""


def _is_link_or_reparse_point(path: Path) -> bool:
    """Identify symbolic links and Windows reparse points."""
    try:
        file_stat = path.lstat()
    except FileNotFoundError:
        return False

    reparse_flag = getattr(stat, "FILE_ATTRIBUTE_REPARSE_POINT", 0)
    attributes = getattr(file_stat, "st_file_attributes", 0)
    return path.is_symlink() or bool(attributes & reparse_flag)


def _reject_link_components(path: Path) -> None:
    """Reject a path if any existing component is a link or reparse point."""
    absolute_path = Path(os.path.abspath(path))
    current = Path(absolute_path.anchor)
    for component in absolute_path.parts[1:]:
        current = current / component
        if _is_link_or_reparse_point(current):
            raise InvalidMediaPath(f"Links and reparse points are not allowed: {current}")


def _is_within(path: Path, root: Path) -> bool:
    """Return whether path is equal to or below root."""
    try:
        path.relative_to(root)
        return True
    except ValueError:
        return False


class MediaFileService:
    """Expose filesystem operations limited to explicit media roots."""

    def __init__(self, media_roots: dict[str, Path], catalog_root: Path) -> None:
        self._media_roots = {
            root_id: Path(root_path) for root_id, root_path in media_roots.items()
        }
        self._catalog_root = Path(catalog_root)

    def configured_roots(self) -> list[dict[str, str]]:
        """Return configured root identifiers without revealing host paths."""
        return [{"root_id": root_id} for root_id in sorted(self._media_roots)]

    def _get_root(self, root_id: str) -> Path:
        """Resolve and validate one configured media root."""
        if not self._media_roots:
            raise MediaConfigurationError(
                "No media roots are configured. Add an absolute path to "
                "MEDIA_ROOTS in media_server/config.py."
            )
        if root_id not in self._media_roots:
            raise MediaConfigurationError(f"Unknown media root: {root_id}")

        root = self._media_roots[root_id]
        if not root.is_absolute():
            raise MediaConfigurationError(
                f"Configured media root '{root_id}' must be an absolute path."
            )
        _reject_link_components(root)
        try:
            resolved_root = root.resolve(strict=True)
        except OSError as error:
            raise MediaConfigurationError(
                f"Configured media root '{root_id}' is unavailable: {error}"
            ) from error
        if not resolved_root.is_dir():
            raise MediaConfigurationError(
                f"Configured media root '{root_id}' is not a directory."
            )
        return resolved_root

    @staticmethod
    def _parse_relative_path(relative_path: str) -> Path:
        """Reject absolute, parent-traversal, and drive-qualified paths."""
        if not isinstance(relative_path, str) or "\0" in relative_path:
            raise InvalidMediaPath("A valid relative path is required.")
        windows_path = PureWindowsPath(relative_path)
        path = Path(relative_path)
        if path.is_absolute() or windows_path.is_absolute() or windows_path.drive:
            raise InvalidMediaPath("Absolute paths are not allowed.")
        if ".." in path.parts or ".." in windows_path.parts:
            raise InvalidMediaPath("Parent-directory traversal is not allowed.")
        return path

    def _resolve_media_path(
        self,
        root_id: str,
        relative_path: str,
        *,
        allow_root: bool = False,
    ) -> tuple[Path, Path]:
        """Resolve a relative path while enforcing its media-root boundary."""
        root = self._get_root(root_id)
        path = self._parse_relative_path(relative_path)
        if not allow_root and (str(path) in ("", ".") or not path.parts):
            raise InvalidMediaPath("A file path is required.")

        candidate = root / path
        _reject_link_components(candidate)
        resolved_candidate = candidate.resolve(strict=False)
        if not _is_within(resolved_candidate, root):
            raise InvalidMediaPath("The requested path is outside its media root.")
        return root, resolved_candidate

    def _get_catalog_root(self) -> Path:
        """Validate the separate catalog destination and its separation."""
        if not self._catalog_root.is_absolute():
            raise MediaConfigurationError(
                "CATALOG_ROOT must be an absolute path."
            )
        _reject_link_components(self._catalog_root)
        catalog_root = self._catalog_root.resolve(strict=False)
        for root_id in self._media_roots:
            media_root = self._get_root(root_id)
            if _is_within(catalog_root, media_root) or _is_within(
                media_root, catalog_root
            ):
                raise MediaConfigurationError(
                    "CATALOG_ROOT must be separate from all media roots."
                )
        return catalog_root

    @staticmethod
    def _file_record(path: Path, relative_path: Path) -> dict[str, Any]:
        """Return basic metadata for one regular file."""
        file_stat = path.stat(follow_symlinks=False)
        return {
            "filename": path.name,
            "relative_path": relative_path.as_posix(),
            "size_bytes": file_stat.st_size,
            "modified_at": datetime.fromtimestamp(
                file_stat.st_mtime, tz=timezone.utc
            ).isoformat(),
        }

    def list_media_files(
        self, root_id: str, directory: str = "."
    ) -> list[dict[str, Any]]:
        """List regular files in one directory, without following links."""
        root, folder = self._resolve_media_path(
            root_id, directory, allow_root=True
        )
        if not folder.is_dir():
            raise InvalidMediaPath("The requested path is not a directory.")

        records: list[dict[str, Any]] = []
        with os.scandir(folder) as entries:
            for entry in sorted(entries, key=lambda item: item.name.casefold()):
                entry_path = Path(entry.path)
                if _is_link_or_reparse_point(entry_path):
                    raise InvalidMediaPath(
                        f"Links and reparse points are not allowed: {entry.name}"
                    )
                if entry.is_file(follow_symlinks=False):
                    relative_path = entry_path.relative_to(root)
                    records.append(self._file_record(entry_path, relative_path))
        return records

    def get_file_info(self, root_id: str, relative_path: str) -> dict[str, Any]:
        """Read metadata for a single regular file."""
        root, file_path = self._resolve_media_path(root_id, relative_path)
        if not file_path.is_file():
            raise InvalidMediaPath("The requested path is not a regular file.")
        return self._file_record(file_path, file_path.relative_to(root))

    def compute_checksum(self, root_id: str, relative_path: str) -> str:
        """Calculate a file's SHA-256 checksum in bounded memory."""
        _, file_path = self._resolve_media_path(root_id, relative_path)
        if not file_path.is_file():
            raise InvalidMediaPath("The requested path is not a regular file.")

        digest = hashlib.sha256()
        with file_path.open("rb") as media_file:
            for chunk in iter(lambda: media_file.read(1024 * 1024), b""):
                digest.update(chunk)
        return digest.hexdigest()

    def _walk_files(self, root: Path) -> Iterator[Path]:
        """Walk regular files, skipping directories marked as non-media."""

        def walk(directory: Path) -> Iterator[Path]:
            with os.scandir(directory) as entries:
                sorted_entries = sorted(entries, key=lambda item: item.name.casefold())
            if any(
                entry.name == "no_media.json"
                and entry.is_file(follow_symlinks=False)
                for entry in sorted_entries
            ):
                return

            for entry in sorted_entries:
                entry_path = Path(entry.path)
                if _is_link_or_reparse_point(entry_path):
                    raise InvalidMediaPath(
                        f"Links and reparse points are not allowed: {entry_path}"
                    )
                if entry.is_dir(follow_symlinks=False):
                    yield from walk(entry_path)
                elif entry.is_file(follow_symlinks=False):
                    yield entry_path

        yield from walk(root)

    def create_inventory(self, root_id: str) -> dict[str, int]:
        """Create missing per-file JSON records in the separate catalog."""
        root = self._get_root(root_id)
        catalog_root = self._get_catalog_root()
        root_catalog = catalog_root / root_id
        _reject_link_components(root_catalog)
        root_catalog.mkdir(parents=True, exist_ok=True)
        _reject_link_components(root_catalog)

        created = 0
        skipped = 0
        for file_path in self._walk_files(root):
            relative_path = file_path.relative_to(root)
            catalog_path = root_catalog / relative_path.parent / (
                relative_path.name + ".json"
            )
            _reject_link_components(catalog_path)
            catalog_path.parent.mkdir(parents=True, exist_ok=True)
            _reject_link_components(catalog_path)

            if catalog_path.exists():
                if not catalog_path.is_file():
                    raise InvalidMediaPath(
                        f"Catalog destination is not a regular file: {catalog_path}"
                    )
                skipped += 1
                continue

            record = self._file_record(file_path, relative_path)
            record["checksum"] = self.compute_checksum(
                root_id, relative_path.as_posix()
            )
            with catalog_path.open("x", encoding="utf-8") as catalog_file:
                json.dump(record, catalog_file, ensure_ascii=False, indent=2)
                catalog_file.write("\n")
            created += 1

        return {"created": created, "skipped_existing": skipped}

    def _move_file(
        self, root_id: str, source_path: str, destination_path: str
    ) -> Path:
        """Move a regular file to a non-existing path in the same media root."""
        root, source = self._resolve_media_path(root_id, source_path)
        destination_root, destination = self._resolve_media_path(
            root_id, destination_path
        )
        if root != destination_root:
            raise InvalidMediaPath("Source and destination roots must match.")
        if source == destination:
            raise InvalidMediaPath("Source and destination must be different.")
        if not source.is_file():
            raise InvalidMediaPath("The source path is not a regular file.")
        if not destination.parent.is_dir():
            raise InvalidMediaPath("The destination directory must already exist.")
        if destination.exists():
            raise InvalidMediaPath("The destination already exists; no overwrite.")

        _reject_link_components(source)
        _reject_link_components(destination)
        if os.name == "nt":
            os.rename(source, destination)
        else:
            os.link(source, destination, follow_symlinks=False)
            source.unlink()
        return destination

    def rename_media_file(
        self, root_id: str, source_path: str, destination_path: str
    ) -> str:
        """Rename a regular file without changing its parent directory."""
        _, source = self._resolve_media_path(root_id, source_path)
        _, destination = self._resolve_media_path(root_id, destination_path)
        if source.parent != destination.parent:
            raise InvalidMediaPath(
                "Rename must stay in the source file's directory; use move instead."
            )
        self._move_file(root_id, source_path, destination_path)
        return destination.relative_to(self._get_root(root_id)).as_posix()

    def move_media_file(
        self, root_id: str, source_path: str, destination_path: str
    ) -> str:
        """Move a regular file within the same configured media root."""
        _, source = self._resolve_media_path(root_id, source_path)
        _, destination = self._resolve_media_path(root_id, destination_path)
        if source.parent == destination.parent:
            raise InvalidMediaPath(
                "Move requires a different directory; use rename instead."
            )
        self._move_file(root_id, source_path, destination_path)
        return destination.relative_to(self._get_root(root_id)).as_posix()
