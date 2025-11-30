# Project Overview
This project allows you to maintain and process all media files.

## Tests
```shell
pytest
```

## Naming convention
All python files named:
- cmd_*.py are designed to be run manually to perform some actions based on media files
- qry_*.py are designed to be run manually to query some information from media files. They do not make any changes to media files

## Verification scripts

## Action scripts

### do_inventory.py
- input parameter: root media folder
  example: `python -m cmd.inventory x:\OneDrive\Osobiste\Media\source`
- walks through all media folders and for each of them creates an inventory.json file which consists of:
  - list of media files
  - checksum of each file
- media folders are all folders without a no_media.json file
