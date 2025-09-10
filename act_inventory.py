

"""
do_inventory.py
----------------
This script recursively scans all subfolders of a specified directory, identifies media folders (folders without a 'no_media.json' file), and creates an 'inventory.json' file in each media folder. The inventory contains a list of all files in the folder and their SHA-256 checksums.

Features:
- Skips folders containing 'no_media.json'.
- For each media folder, lists all files and computes their checksums.
- Writes results to 'inventory.json' in each folder (unless dry mode is enabled).
- Prints progress information for folders and files.
- Supports a dry mode to preview changes without modifying any files.

Usage:
    python do_inventory.py <directory> [--dry]

Arguments:
    <directory>   The root directory to scan for media folders.
    --dry         If specified, no files are modified; instead, the script displays what would change.

Requirements:
    - Python 3.6+
    - Standard library only
"""

import os
import sys
import json
import hashlib
from typing import List, Dict

INVENTORY_FILENAME: str = "inventory.json"
NO_MEDIA_FILENAME: str = "no_media.json"


def compute_checksum(file_path: str, chunk_size: int = 65536) -> str:
    """
    Compute the SHA-256 checksum of a file.

    Args:
        file_path (str): Path to the file.
        chunk_size (int): Number of bytes to read at a time (default: 65536).

    Returns:
        str: The SHA-256 checksum as a hex string, or an error message if reading fails.
    """
    sha256 = hashlib.sha256()
    try:
        with open(file_path, "rb") as f:
            for chunk in iter(lambda: f.read(chunk_size), b""):
                sha256.update(chunk)
        return sha256.hexdigest()
    except Exception as e:
        return f"ERROR: {e}"


def is_media_folder(folder_path: str) -> bool:
    """
    Determine if a folder is a media folder (does not contain 'no_media.json').

    Args:
        folder_path (str): Path to the folder.

    Returns:
        bool: True if the folder is a media folder, False otherwise.
    """
    return not os.path.exists(os.path.join(folder_path, NO_MEDIA_FILENAME))


def get_media_files(folder_path: str) -> List[str]:
    """
    List all files in a folder (non-recursive).

    Args:
        folder_path (str): Path to the folder.

    Returns:
        List[str]: List of filenames in the folder.
    """
    files: List[str] = []
    for entry in os.scandir(folder_path):
        if entry.is_file():
            files.append(entry.name)
    return files



# --- Inventory Writers ---
class InventoryWriter:
    def write(self, folder_path: str, inventory: List[Dict[str, str]]):
        inventory_path = os.path.join(folder_path, INVENTORY_FILENAME)
        with open(inventory_path, "w", encoding="utf-8") as f:
            json.dump(inventory, f, indent=2)
        print(f"Inventory created for {folder_path} ({len(inventory)} files)")

class DryInventoryWriter:
    def write(self, folder_path: str, inventory: List[Dict[str, str]]):
        inventory_path = os.path.join(folder_path, INVENTORY_FILENAME)
        print(f"[DRY MODE] Would write inventory to: {inventory_path}")
        print(json.dumps(inventory, indent=2))

def create_inventory(folder_path: str, writer) -> None:
    """
    Create an inventory.json file in the given folder, listing all files and their checksums.
    Uses the provided writer to perform the actual write or dry-run.

    Args:
        folder_path (str): Path to the media folder.
        writer: An object with a .write(folder_path, inventory) method.

    Returns:
        None
    """
    media_files: List[str] = get_media_files(folder_path)
    inventory: List[Dict[str, str]] = []
    total_files = len(media_files)
    print(f"Creating inventory for {folder_path} ({total_files} files)...")
    for idx, filename in enumerate(media_files, 1):
        file_path = os.path.join(folder_path, filename)
        print(f"  [{idx}/{total_files}] Processing: {filename}")
        checksum = compute_checksum(file_path)
        inventory.append({
            "filename": filename,
            "checksum": checksum
        })
    writer.write(folder_path, inventory)


def walk_and_inventory(root_folder: str, dry: bool = False) -> None:
    """
    Recursively walk through all subfolders of root_folder, creating inventories for media folders.
    Uses the appropriate writer for dry or real mode.

    Args:
        root_folder (str): Path to the root folder to scan.
        dry (bool): If True, do not write files, just display changes.

    Returns:
        None
    """
    print(f"Scanning for media folders in {root_folder}...{' [DRY MODE]' if dry else ''}")
    folder_count = 0
    writer = DryInventoryWriter() if dry else InventoryWriter()
    for dirpath, dirnames, filenames in os.walk(root_folder):
        if is_media_folder(dirpath):
            folder_count += 1
            print(f"\n[{folder_count}] Media folder: {dirpath}")
            create_inventory(dirpath, writer)
        else:
            print(f"Skipping {dirpath} (no_media.json present)")
    print(f"\nProcessed {folder_count} media folders.")


def main() -> None:
    """
    Main entry point. Scans the specified directory (or the parent of the current working directory if not specified) for media folders and creates inventories.
    """
    dry = False
    args = sys.argv[1:]
    if not args:
        print("Usage: python do_inventory.py <directory> [--dry]")
        print("Please provide the entry directory to scan for media folders.")
        print("Add --dry to preview changes without modifying files.")
        return
    if '--dry' in args:
        dry = True
        args.remove('--dry')
    root_folder: str = os.path.abspath(args[0]) if args else None
    if not root_folder:
        print("Usage: python do_inventory.py <directory> [--dry]")
        return
    walk_and_inventory(root_folder, dry=dry)


if __name__ == "__main__":
    main()
