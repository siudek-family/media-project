

"""
inventory.py
----------------
This script recursively scans all subfolders in a source directory, processes media files, and creates corresponding .json files in a target directory maintaining the same folder structure.

Features:
- Reads from source directory (read-only) as source of raw media data
- Writes to target directory as root directory for produced files
- For each subfolder in source, creates corresponding subfolder in target
- For each file in source subfolders, creates a .json file with the same name in target
- Uses walk_and_inventory to process folders and create inventories
- Supports a dry mode to preview changes without modifying any files

Usage:
    python cmd_inventory.py <source_dir> <target_dir> [--dry]

Arguments:
    source_dir    Path to the source directory containing media files
    target_dir    Path to the target directory where JSON files will be created
    --dry         If specified, no files are modified; instead, the script displays what would change.

Requirements:
    - Python 3.11+
    - Standard library only
"""

import os
import sys
import json
import hashlib

INVENTORY_FILENAME: str = "inventory.json"


# --- Application Exceptions ---
class InventoryError(Exception):
    """Base exception for inventory-related errors."""
    pass


class ChecksumError(InventoryError):
    """Exception raised when checksum computation fails."""
    pass


class PathValidationError(InventoryError):
    """Exception raised when path validation fails."""
    pass


def compute_checksum(file_path: str, chunk_size: int = 65536) -> str:
    """
    Compute the SHA-256 checksum of a file.

    Args:
        file_path (str): Path to the file.
        chunk_size (int): Number of bytes to read at a time (default: 65536).

    Returns:
        str: The SHA-256 checksum as a hex string.
        
    Raises:
        ChecksumError: If the file cannot be read or checksum computation fails.
    """
    sha256 = hashlib.sha256()
    try:
        with open(file_path, "rb") as f:
            for chunk in iter(lambda: f.read(chunk_size), b""):
                sha256.update(chunk)
        return sha256.hexdigest()
    except Exception as e:
        raise ChecksumError(f"Failed to compute checksum for '{file_path}': {e}") from e


def get_media_files(folder_path: str) -> list[str]:
    """
    List all files in a folder (non-recursive).

    Args:
        folder_path (str): Path to the folder.

    Returns:
        list[str]: List of filenames in the folder.
    """
    files: list[str] = []
    for entry in os.scandir(folder_path):
        if entry.is_file():
            files.append(entry.name)
    return files



# --- Inventory Writers ---
class InventoryWriter:
    def __init__(self, source_root: str, target_root: str):
        self.source_root = source_root
        self.target_root = target_root
    
    def write(self, source_folder_path: str, inventory: list[dict[str, str]]):
        # Validate that source_folder_path is within source_root
        # Normalize both paths to resolve any .. components
        normalized_source_root = os.path.abspath(self.source_root)
        normalized_source_folder = os.path.abspath(source_folder_path)
        
        # Check if the normalized source folder path starts with the normalized source root
        if not normalized_source_folder.startswith(normalized_source_root + os.sep) and normalized_source_folder != normalized_source_root:
            raise PathValidationError(f"Source folder path '{source_folder_path}' is not within source root '{self.source_root}'")
        
        # Calculate relative path from source root
        rel_path = os.path.relpath(normalized_source_folder, normalized_source_root)
        target_folder_path = os.path.join(self.target_root, rel_path)
        
        # Create target directory if it doesn't exist
        os.makedirs(target_folder_path, exist_ok=True)
        
        # Create .json files for each media file
        created_count = 0
        skipped_count = 0
        for file_info in inventory:
            filename = file_info["filename"]
            json_filename = filename + ".json"
            json_path = os.path.join(target_folder_path, json_filename)
            
            # Skip if JSON file already exists
            if os.path.exists(json_path):
                skipped_count += 1
                continue
                
            with open(json_path, "w", encoding="utf-8") as f:
                json.dump(file_info, f, indent=2)
            created_count += 1
        
        if created_count > 0:
            print(f"Created {created_count} JSON files in {target_folder_path}")
        if skipped_count > 0:
            print(f"Skipped {skipped_count} existing JSON files in {target_folder_path}")

class DryInventoryWriter:
    def __init__(self, source_root: str, target_root: str):
        self.source_root = source_root
        self.target_root = target_root
    
    def write(self, source_folder_path: str, inventory: list[dict[str, str]]):
        # Validate that source_folder_path is within source_root
        # Normalize both paths to resolve any .. components
        normalized_source_root = os.path.abspath(self.source_root)
        normalized_source_folder = os.path.abspath(source_folder_path)
        
        # Check if the normalized source folder path starts with the normalized source root
        if not normalized_source_folder.startswith(normalized_source_root + os.sep) and normalized_source_folder != normalized_source_root:
            raise PathValidationError(f"Source folder path '{source_folder_path}' is not within source root '{self.source_root}'")
        
        # Calculate relative path from source root
        rel_path = os.path.relpath(normalized_source_folder, normalized_source_root)
        target_folder_path = os.path.join(self.target_root, rel_path)
        
        print(f"[DRY MODE] Would create directory: {target_folder_path}")
        would_create_count = 0
        would_skip_count = 0
        for file_info in inventory:
            filename = file_info["filename"]
            json_filename = filename + ".json"
            json_path = os.path.join(target_folder_path, json_filename)
            
            # Check if JSON file already exists
            if os.path.exists(json_path):
                print(f"[DRY MODE] Would skip existing: {json_path}")
                would_skip_count += 1
            else:
                print(f"[DRY MODE] Would write: {json_path}")
                print(json.dumps(file_info, indent=2))
                would_create_count += 1
        
        if would_create_count > 0:
            print(f"[DRY MODE] Would create {would_create_count} JSON files")
        if would_skip_count > 0:
            print(f"[DRY MODE] Would skip {would_skip_count} existing JSON files")

def create_inventory(folder_path: str, writer) -> None:
    """
    Create inventory for files in the given folder, using the provided writer to create .json files.

    Args:
        folder_path (str): Path to the source media folder.
        writer: An object with a .write(source_folder_path, inventory) method.

    Returns:
        None
    """
    media_files: list[str] = get_media_files(folder_path)
    inventory: list[dict[str, str]] = []
    total_files = len(media_files)
    print(f"Creating inventory for {folder_path} ({total_files} files)...")
    for idx, filename in enumerate(media_files, 1):
        file_path = os.path.join(folder_path, filename)
        print(f"  [{idx}/{total_files}] Processing: {filename}")
        try:
            checksum = compute_checksum(file_path)
        except ChecksumError as e:
            print(f"    WARNING: {e}")
            checksum = f"ERROR: {e}"
        inventory.append({
            "filename": filename,
            "checksum": checksum
        })
    writer.write(folder_path, inventory)


def walk_and_inventory(source_root: str, target_root: str, dry: bool) -> None:
    """
    Recursively walk through all subfolders of source_root, creating corresponding JSON files in target_root.
    For each file in source subdirectories, creates a .json file with the same name in the corresponding target subdirectory.

    Args:
        source_root (str): Path to the source root folder to scan (read-only).
        target_root (str): Path to the target root folder where JSON files will be created.
        dry (bool): If True, do not write files, just display changes.

    Returns:
        None
    """
    print(f"Scanning source folders in {source_root}...{' [DRY MODE]' if dry else ''}")
    print(f"Target root: {target_root}")
    folder_count = 0
    writer = DryInventoryWriter(source_root, target_root) if dry else InventoryWriter(source_root, target_root)
    
    for dirpath, dirnames, filenames in os.walk(source_root):
        # Only process directories that have files
        if filenames:
            folder_count += 1
            print(f"\n[{folder_count}] Processing folder: {dirpath}")
            create_inventory(dirpath, writer)
    
    print(f"\nProcessed {folder_count} folders.")


def main() -> None:
    """
    Main entry point. Scans source directory for media files and creates corresponding .json files in target directory.
    
    Usage:
        python -m cmd.inventory.py <source_dir> <target_dir> [--dry]
    """
    dry = False
    args = sys.argv[1:]
    
    if '--dry' in args:
        dry = True
        args.remove('--dry')
    
    # Check for required arguments
    if len(args) != 2:
        print("Usage: python -m cmd.inventory.py <source_dir> <target_dir> [--dry]")
        print()
        print("Arguments:")
        print("  source_dir    Path to the source directory containing media files")
        print("  target_dir    Path to the target directory where JSON files will be created")
        print("  --dry         Optional flag to preview changes without writing files")
        return
    
    source_root, target_root = args
    
    # Normalize paths
    source_root = os.path.abspath(source_root)
    target_root = os.path.abspath(target_root)
    
    # Check if source directory exists
    if not os.path.exists(source_root):
        print(f"Error: Source directory does not exist: {source_root}")
        print("Please provide a valid source directory path.")
        return
    
    # Create target directory if it doesn't exist (in non-dry mode)
    if not dry:
        os.makedirs(target_root, exist_ok=True)
    
    print(f"Source directory: {source_root}")
    print(f"Target directory: {target_root}")
    
    walk_and_inventory(source_root, target_root, dry=dry)


if __name__ == "__main__":
    main()
