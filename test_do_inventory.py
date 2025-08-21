import os
import tempfile
import shutil
import json
import pytest
from pathlib import Path
from typing import Any
from do_inventory import compute_checksum, is_media_folder, get_media_files, create_inventory

def test_compute_checksum(tmp_path: Path) -> None:
    file_path: Path = tmp_path / "test.txt"
    file_path.write_text("hello world")
    checksum: str = compute_checksum(str(file_path))
    assert isinstance(checksum, str)
    # Known sha256 for 'hello world'
    assert checksum == "b94d27b9934d3e08a52e52d7da7dabfac484efe37a5380ee9088f7ace2efcde9"

def test_is_media_folder(tmp_path: Path) -> None:
    # Should be True if no no_media.json
    assert is_media_folder(str(tmp_path))
    # Should be False if no_media.json exists
    (tmp_path / "no_media.json").write_text("")
    assert not is_media_folder(str(tmp_path))

def test_get_media_files(tmp_path: Path) -> None:
    (tmp_path / "a.txt").write_text("a")
    (tmp_path / "b.txt").write_text("b")
    os.mkdir(tmp_path / "subfolder")
    files: list[str] = get_media_files(str(tmp_path))
    assert set(files) == {"a.txt", "b.txt"}

def test_create_inventory(tmp_path: Path) -> None:
    (tmp_path / "file1.txt").write_text("abc")
    (tmp_path / "file2.txt").write_text("def")
    create_inventory(str(tmp_path))
    inventory_path: Path = tmp_path / "inventory.json"
    assert inventory_path.exists()
    with open(inventory_path) as f:
        data: list[dict[str, Any]] = json.load(f)
    filenames: set[str] = {item["filename"] for item in data}
    assert filenames == {"file1.txt", "file2.txt"}
    for item in data:
        assert "checksum" in item
