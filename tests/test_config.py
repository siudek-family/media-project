import json
from pathlib import Path

import pytest

from media_server.config import load_media_service, parse_media_configuration
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


def test_parses_paths_without_requiring_existing_directories(tmp_path: Path) -> None:
    media_root = tmp_path / "media"
    catalog_root = tmp_path / "catalog"

    config = parse_media_configuration(
        {
            "media_roots": {"family": str(media_root)},
            "catalog_root": str(catalog_root),
        }
    )

    assert config.media_roots == {"family": media_root}
    assert config.catalog_root == catalog_root


@pytest.mark.parametrize(
    ("data", "message"),
    [
        ([], "must contain exactly"),
        ({"media_roots": {"family": "/media"}}, "must contain exactly"),
        (
            {"media_roots": {}, "catalog_root": "/catalog", "extra": True},
            "must contain exactly",
        ),
        ({"media_roots": [], "catalog_root": "/catalog"}, "must be a JSON object"),
        ({"media_roots": {}, "catalog_root": "/catalog"}, "No media roots"),
        (
            {"media_roots": {"": "/media"}, "catalog_root": "/catalog"},
            "non-empty string identifier",
        ),
        (
            {"media_roots": {"family": 42}, "catalog_root": "/catalog"},
            "string path",
        ),
        (
            {"media_roots": {"family": "/media"}, "catalog_root": None},
            "'catalog_root' must be a string path",
        ),
    ],
)
def test_rejects_invalid_configuration_structure(data: object, message: str) -> None:
    with pytest.raises(MediaConfigurationError, match=message):
        parse_media_configuration(data)


def test_loader_rejects_empty_media_roots(tmp_path: Path) -> None:
    catalog_root = tmp_path / "catalog"
    catalog_root.mkdir()
    config_path = tmp_path / "config.json"
    config_path.write_text(
        json.dumps({"media_roots": {}, "catalog_root": str(catalog_root)}),
        encoding="utf-8",
    )

    with pytest.raises(MediaConfigurationError, match="No media roots"):
        load_media_service(config_path)


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
