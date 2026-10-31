"""Load and validate the local JSON configuration for the media server."""

import json
from dataclasses import dataclass
from pathlib import Path

from media_server.filesystem import MediaConfigurationError, MediaFileService

CONFIG_PATH: Path = Path(__file__).with_name("config.json")


@dataclass(frozen=True)
class MediaConfiguration:
    """Typed media paths, independent of filesystem availability."""

    media_roots: dict[str, Path]
    catalog_root: Path


def parse_media_configuration(data: object) -> MediaConfiguration:
    """Validate the configuration structure and map its paths."""
    if not isinstance(data, dict) or set(data) != {
        "media_roots",
        "catalog_root",
    }:
        raise MediaConfigurationError(
            "Configuration must contain exactly 'media_roots' and 'catalog_root'."
        )

    raw_roots = data["media_roots"]
    if not isinstance(raw_roots, dict):
        raise MediaConfigurationError("'media_roots' must be a JSON object.")
    if not raw_roots:
        raise MediaConfigurationError("No media roots are configured.")

    media_roots: dict[str, Path] = {}
    for root_id, root_path in raw_roots.items():
        if (
            not isinstance(root_id, str)
            or not root_id
            or not isinstance(root_path, str)
        ):
            raise MediaConfigurationError(
                "Each media root must have a non-empty string identifier "
                "and a string path."
            )
        media_roots[root_id] = Path(root_path)

    catalog_path = data["catalog_root"]
    if not isinstance(catalog_path, str):
        raise MediaConfigurationError("'catalog_root' must be a string path.")

    return MediaConfiguration(media_roots, Path(catalog_path))


def load_media_service(config_path: Path = CONFIG_PATH) -> MediaFileService:
    """Load configuration and fail if any configured directory is unavailable."""
    try:
        with config_path.open(encoding="utf-8") as config_file:
            data: object = json.load(config_file)
    except OSError as error:
        raise MediaConfigurationError(
            f"Cannot read media server configuration at {config_path}: {error}"
        ) from error
    except json.JSONDecodeError as error:
        raise MediaConfigurationError(
            f"Invalid JSON in media server configuration at {config_path}: {error}"
        ) from error

    config = parse_media_configuration(data)
    service = MediaFileService(config.media_roots, config.catalog_root)
    service.validate_configuration()
    return service
