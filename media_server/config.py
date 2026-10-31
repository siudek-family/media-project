"""Load and validate the local JSON configuration for the media server."""

import json
from pathlib import Path
from typing import Any

from media_server.filesystem import MediaConfigurationError, MediaFileService

CONFIG_PATH = Path(__file__).with_name("config.json")


def load_media_service(
    config_path: Path = CONFIG_PATH,
) -> MediaFileService:
    """Load configuration and fail if any configured directory is unavailable."""
    try:
        with config_path.open(encoding="utf-8") as config_file:
            config: Any = json.load(config_file)
    except OSError as error:
        raise MediaConfigurationError(
            f"Cannot read media server configuration at {config_path}: {error}"
        ) from error
    except json.JSONDecodeError as error:
        raise MediaConfigurationError(
            f"Invalid JSON in media server configuration at {config_path}: {error}"
        ) from error

    if not isinstance(config, dict) or set(config) != {
        "media_roots",
        "catalog_root",
    }:
        raise MediaConfigurationError(
            "Configuration must contain exactly 'media_roots' and 'catalog_root'."
        )

    raw_roots = config["media_roots"]
    if not isinstance(raw_roots, dict):
        raise MediaConfigurationError("'media_roots' must be a JSON object.")

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

    catalog_path = config["catalog_root"]
    if not isinstance(catalog_path, str):
        raise MediaConfigurationError("'catalog_root' must be a string path.")

    service = MediaFileService(media_roots, Path(catalog_path))
    service.validate_configuration()
    return service
