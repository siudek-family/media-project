# Copilot Instructions for AI Coding Agents

## Project Purpose
This project manages and verifies media files by generating inventories and checksums for each media folder. It is designed for periodic verification and maintenance of media collections.

## Key Components
- `do_inventory.py`: Walks through all media folders, creating an `inventory.json` with a list of media files and their checksums. Media folders are those **without** a `no_media.json` file.
- `ver_*.py`: Any Python file matching this pattern is a verification script, intended to be run periodically to check media integrity and display action points.
- `test_do_inventory.py`: Contains tests for the main inventory logic.

## Developer Workflows
- **Run all tests:**
  ```shell
  pytest
  ```
- **Add new verification scripts:**
  Name them as `ver_*.py` and ensure they output actionable results.

## Project Conventions
- Media folders are identified by the absence of a `no_media.json` file.
- Inventories are stored as `inventory.json` in each media folder, containing file lists and checksums.
- Keep all scripts in the project root for simplicity.

## External Dependencies
- All dependencies are listed in `requirements.txt`. Install with:
  ```shell
  pip install -r requirements.txt
  ```

## Examples
- To update inventories, run `do_inventory.py`.
- To add a new check, create a new `ver_*.py` script following the naming convention.

## References
- See `README.md` for a summary and workflow details.
- See `do_inventory.py` for the main inventory logic and data flow.
- See `test_do_inventory.py` for test structure and examples.
