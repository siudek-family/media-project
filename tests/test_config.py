import json
from pathlib import Path

import pytest

from media_server.config import load_media_service
from media_server.filesystem import MediaConfigurationError


def _write_config(
    config_path: Path, media_root: Path, catalog_root: Path
) -> None:
    config_path.write_text(
        json.dumps(
            {
                "media_roots": {"family": str(media_root)},
                "catalog_root": str(catalog_root),
            }
        ),
        encoding="utf-8",
    )


def test_loads_valid_config_with_existing_directories(tmp_path: Path) -> None:
    media_root = tmp_path / "media"
    media_root.mkdir()
    catalog_root = tmp_path / "catalog"
    catalog_root.mkdir()
    config_path = tmp_path / "config.json"
    _write_config(config_path, media_root, catalog_root)

    service = load_media_service(config_path)

    assert service.configured_roots() == [{"root_id": "family"}]


@pytest.mark.parametrize("missing_directory", ["media", "catalog"])
def test_fails_fast_when_configured_directory_is_missing(
    tmp_path: Path, missing_directory: str
) -> None:
    media_root = tmp_path / "media"
    catalog_root = tmp_path / "catalog"
    media_root.mkdir()
    catalog_root.mkdir()
    if missing_directory == "media":
        media_root.rmdir()
    else:
        catalog_root.rmdir()
    config_path = tmp_path / "config.json"
    _write_config(config_path, media_root, catalog_root)

    with pytest.raises(MediaConfigurationError, match="unavailable"):
        load_media_service(config_path)
