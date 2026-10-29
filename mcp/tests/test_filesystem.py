import hashlib
import json
import stat
from pathlib import Path
from types import SimpleNamespace

import pytest

from media_server import filesystem
from media_server.filesystem import (
    InvalidMediaPath,
    MediaConfigurationError,
    MediaFileService,
)


@pytest.fixture
def service(tmp_path: Path) -> MediaFileService:
    media_root = tmp_path / "media"
    media_root.mkdir()
    catalog_root = tmp_path / "catalog"
    return MediaFileService({"family": media_root}, catalog_root)


def test_no_roots_fails_closed(tmp_path: Path) -> None:
    service = MediaFileService({}, tmp_path / "catalog")
    with pytest.raises(MediaConfigurationError, match="No media roots"):
        service.list_media_files("family")


def test_list_and_read_file_metadata(
    service: MediaFileService, tmp_path: Path
) -> None:
    media_file = tmp_path / "media" / "photo.jpg"
    media_file.write_bytes(b"image-data")

    files = service.list_media_files("family")
    info = service.get_file_info("family", "photo.jpg")

    assert [item["filename"] for item in files] == ["photo.jpg"]
    assert info["relative_path"] == "photo.jpg"
    assert info["size_bytes"] == len(b"image-data")
    assert "modified_at" in info


def test_checksum_is_sha256(service: MediaFileService, tmp_path: Path) -> None:
    (tmp_path / "media" / "photo.jpg").write_bytes(b"image-data")
    expected = hashlib.sha256(b"image-data").hexdigest()

    assert service.compute_checksum("family", "photo.jpg") == expected


@pytest.mark.parametrize(
    "path",
    [
        "../outside.txt",
        "nested/../../outside.txt",
        r"C:\Windows\win.ini",
        r"\\server\share\file",
        "/etc/passwd",
    ],
)
def test_rejects_absolute_and_traversal_paths(
    service: MediaFileService, path: str
) -> None:
    with pytest.raises(InvalidMediaPath):
        service.get_file_info("family", path)


def test_inventory_writes_separate_records_and_skips_no_media(
    service: MediaFileService, tmp_path: Path
) -> None:
    media_root = tmp_path / "media"
    (media_root / "photo.jpg").write_bytes(b"photo")
    excluded = media_root / "excluded"
    excluded.mkdir()
    (excluded / "no_media.json").write_text("{}", encoding="utf-8")
    (excluded / "secret.jpg").write_bytes(b"secret")

    result = service.create_inventory("family")
    record_path = tmp_path / "catalog" / "family" / "photo.jpg.json"
    record = json.loads(record_path.read_text(encoding="utf-8"))

    assert result == {"created": 1, "skipped_existing": 0}
    assert record["relative_path"] == "photo.jpg"
    assert record["checksum"] == hashlib.sha256(b"photo").hexdigest()
    assert not (tmp_path / "catalog" / "family" / "excluded").exists()
    assert (media_root / "photo.jpg").read_bytes() == b"photo"


def test_inventory_does_not_overwrite_existing_record(
    service: MediaFileService, tmp_path: Path
) -> None:
    source = tmp_path / "media" / "photo.jpg"
    source.write_bytes(b"first")
    record = tmp_path / "catalog" / "family" / "photo.jpg.json"
    record.parent.mkdir(parents=True)
    record.write_text("keep", encoding="utf-8")

    assert service.create_inventory("family") == {
        "created": 0,
        "skipped_existing": 1,
    }
    assert record.read_text(encoding="utf-8") == "keep"


def test_catalog_cannot_overlap_media_root(tmp_path: Path) -> None:
    media_root = tmp_path / "media"
    media_root.mkdir()
    service = MediaFileService({"family": media_root}, media_root / "catalog")

    with pytest.raises(MediaConfigurationError, match="separate"):
        service.create_inventory("family")


def test_reparse_points_are_rejected_on_windows_too(
    monkeypatch: pytest.MonkeyPatch,
) -> None:
    monkeypatch.setattr(
        Path,
        "lstat",
        lambda self: SimpleNamespace(st_file_attributes=0x400),
    )
    monkeypatch.setattr(Path, "is_symlink", lambda self: False)
    monkeypatch.setattr(
        stat, "FILE_ATTRIBUTE_REPARSE_POINT", 0x400, raising=False
    )

    assert filesystem._is_link_or_reparse_point(Path("junction"))


def test_rejects_symlinked_file(
    service: MediaFileService, tmp_path: Path
) -> None:
    outside = tmp_path / "outside.txt"
    outside.write_text("private", encoding="utf-8")
    link = tmp_path / "media" / "linked.txt"
    try:
        link.symlink_to(outside)
    except (OSError, NotImplementedError):
        pytest.skip("Symlink creation is not available.")

    with pytest.raises(InvalidMediaPath, match="Links"):
        service.get_file_info("family", "linked.txt")


def test_rename_stays_in_same_directory(
    service: MediaFileService, tmp_path: Path
) -> None:
    source = tmp_path / "media" / "before.jpg"
    source.write_bytes(b"photo")

    assert service.rename_media_file("family", "before.jpg", "after.jpg") == (
        "after.jpg"
    )
    assert not source.exists()
    assert (tmp_path / "media" / "after.jpg").read_bytes() == b"photo"


def test_move_stays_in_same_root_and_requires_existing_directory(
    service: MediaFileService, tmp_path: Path
) -> None:
    media_root = tmp_path / "media"
    target = media_root / "album"
    target.mkdir()
    source = media_root / "photo.jpg"
    source.write_bytes(b"photo")

    assert service.move_media_file("family", "photo.jpg", "album/photo.jpg") == (
        "album/photo.jpg"
    )
    assert not source.exists()
    assert (target / "photo.jpg").read_bytes() == b"photo"


def test_rename_rejects_different_directory(
    service: MediaFileService, tmp_path: Path
) -> None:
    media_root = tmp_path / "media"
    (media_root / "album").mkdir()
    (media_root / "photo.jpg").write_bytes(b"photo")

    with pytest.raises(InvalidMediaPath, match="use move"):
        service.rename_media_file("family", "photo.jpg", "album/photo.jpg")


def test_move_rejects_overwrite(
    service: MediaFileService, tmp_path: Path
) -> None:
    media_root = tmp_path / "media"
    (media_root / "album").mkdir()
    (media_root / "photo.jpg").write_bytes(b"source")
    (media_root / "album" / "photo.jpg").write_bytes(b"target")

    with pytest.raises(InvalidMediaPath, match="no overwrite"):
        service.move_media_file("family", "photo.jpg", "album/photo.jpg")

    assert (media_root / "photo.jpg").read_bytes() == b"source"
    assert (media_root / "album" / "photo.jpg").read_bytes() == b"target"


def test_rename_rejects_same_source_and_destination(
    service: MediaFileService, tmp_path: Path
) -> None:
    (tmp_path / "media" / "photo.jpg").write_bytes(b"photo")

    with pytest.raises(InvalidMediaPath, match="must be different"):
        service.rename_media_file("family", "photo.jpg", "photo.jpg")


def test_rejects_symlinked_directory_during_inventory(
    service: MediaFileService, tmp_path: Path
) -> None:
    outside = tmp_path / "outside"
    outside.mkdir()
    (outside / "photo.jpg").write_bytes(b"private")
    try:
        (tmp_path / "media" / "linked").symlink_to(outside, target_is_directory=True)
    except (OSError, NotImplementedError):
        pytest.skip("Symlink creation is not available.")

    with pytest.raises(InvalidMediaPath, match="Links"):
        service.create_inventory("family")
