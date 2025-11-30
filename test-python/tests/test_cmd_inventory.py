import os
import tempfile
import shutil
import json
import pytest
from pathlib import Path
from typing import Any
import sys
from cmd.inventory import compute_checksum, get_media_files, create_inventory, InventoryWriter, DryInventoryWriter, walk_and_inventory, main, ChecksumError, PathValidationError

def test_compute_checksum(tmp_path: Path) -> None:
    file_path: Path = tmp_path / "test.txt"
    file_path.write_text("hello world")
    checksum: str = compute_checksum(file_path)
    assert isinstance(checksum, str)
    # Known sha256 for 'hello world'
    assert checksum == "b94d27b9934d3e08a52e52d7da7dabfac484efe37a5380ee9088f7ace2efcde9"

def test_get_media_files(tmp_path: Path) -> None:
    (tmp_path / "a.txt").write_text("a")
    (tmp_path / "b.txt").write_text("b")
    os.mkdir(tmp_path / "subfolder")
    files: list[str] = get_media_files(tmp_path)
    assert set(files) == {"a.txt", "b.txt"}

@pytest.mark.parametrize("dry_mode", [False, True])
def test_create_inventory(tmp_path: Path, capsys, dry_mode: bool) -> None:
    """Test create_inventory function in both regular and dry modes, including subdirectory handling."""
    source_path = tmp_path / "source"
    source_path.mkdir()
    
    # Create files in source root
    (source_path / "file1.txt").write_text("abc")
    (source_path / "file2.txt").write_text("def")
    
    # Create subdirectory with files
    subdir = source_path / "subdir"
    subdir.mkdir()
    (subdir / "file3.txt").write_text("ghi")
    
    target_path = tmp_path / "target"
    
    # Choose writer based on mode
    writer = DryInventoryWriter(str(source_path), str(target_path)) if dry_mode else InventoryWriter(str(source_path), str(target_path))
    
    # Test both root directory and subdirectory
    create_inventory(str(source_path), writer)
    create_inventory(str(subdir), writer)
    
    if dry_mode:
        # In dry mode, no files should be created
        assert not target_path.exists()
        captured = capsys.readouterr()
        assert "[DRY MODE] Would create directory" in captured.out
        assert "[DRY MODE] Would write:" in captured.out
    else:
        # In regular mode, JSON files should be created in both root and subdirectory
        assert (target_path / "file1.txt.json").exists()
        assert (target_path / "file2.txt.json").exists()
        assert (target_path / "subdir" / "file3.txt.json").exists()
        
        # Check content of files from both root and subdirectory
        with open(target_path / "file1.txt.json") as f:
            data: dict[str, Any] = json.load(f)
        assert data["filename"] == "file1.txt"
        assert "checksum" in data
        
        with open(target_path / "subdir" / "file3.txt.json") as f:
            data: dict[str, Any] = json.load(f)
        assert data["filename"] == "file3.txt"
        assert "checksum" in data

def test_inventory_writer_path_traversal_protection(tmp_path: Path) -> None:
    """Test that InventoryWriter prevents path traversal attacks."""
    source_root = tmp_path / "source"
    source_root.mkdir()
    target_root = tmp_path / "target"
    
    writer = InventoryWriter(str(source_root), str(target_root))
    inventory = [{"filename": "malicious.txt", "checksum": "abc123"}]
    
    # Test various path traversal attempts
    malicious_paths = [
        str(tmp_path / "outside"),  # Completely outside
        str(source_root / ".." / "outside"),  # Using .. to escape
        str(source_root / "valid" / ".." / ".." / "outside"),  # Multiple .. to escape
        "/etc/passwd",  # Absolute path (Unix)
        "C:\\Windows\\System32",  # Absolute path (Windows)
    ]
    
    for malicious_path in malicious_paths:
        with pytest.raises(PathValidationError, match="is not within source root"):
            writer.write(malicious_path, inventory)

def test_compute_checksum_error():
    # File does not exist
    with pytest.raises(ChecksumError, match="Failed to compute checksum"):
        compute_checksum("nonexistent_file.txt")

def test_inventory_writer_empty_inventory(tmp_path: Path) -> None:
    """Test that InventoryWriter handles empty inventory correctly."""
    source_root = tmp_path / "source"
    source_root.mkdir()
    target_root = tmp_path / "target"
    
    writer = InventoryWriter(str(source_root), str(target_root))
    writer.write(str(source_root), [])  # Empty inventory
    
    # Target directory should be created even for empty inventory
    assert target_root.exists()
    # But no JSON files should exist
    assert len(list(target_root.glob("*.json"))) == 0

def test_walk_and_inventory_integration(tmp_path: Path) -> None:
    """Integration test for walk_and_inventory function."""
    source_root = tmp_path / "source"
    target_root = tmp_path / "target"
    
    # Create a complex directory structure
    source_root.mkdir()
    (source_root / "file1.txt").write_text("content1")
    
    subdir1 = source_root / "subdir1"
    subdir1.mkdir()
    (subdir1 / "file2.txt").write_text("content2")
    
    subdir2 = source_root / "subdir1" / "subdir2"
    subdir2.mkdir()
    (subdir2 / "file3.txt").write_text("content3")
    
    # Empty directory - should be skipped
    empty_dir = source_root / "empty"
    empty_dir.mkdir()
    
    walk_and_inventory(str(source_root), str(target_root), dry=False)
    
    # Check that all expected JSON files were created
    assert (target_root / "file1.txt.json").exists()
    assert (target_root / "subdir1" / "file2.txt.json").exists()
    assert (target_root / "subdir1" / "subdir2" / "file3.txt.json").exists()
    
    # Check that empty directory was not created in target
    assert not (target_root / "empty").exists()
    
    # Verify content of one JSON file
    with open(target_root / "file1.txt.json") as f:
        data = json.load(f)
    assert data["filename"] == "file1.txt"
    assert "checksum" in data

def test_main_no_args(monkeypatch, capsys):
    monkeypatch.setattr(sys, "argv", ["cmd_inventory.py"])
    main()
    captured = capsys.readouterr()
    assert "Usage: python cmd_inventory.py <source_dir> <target_dir> [--dry]" in captured.out
