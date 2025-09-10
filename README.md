# Project Overview
This project allows to maintain and verify all media files.

## Tests
```shell
pytest
```

## Naming convention
All python files named"
- ver_*.py are designed to be run periodically to do some checks and display results and potential action points
- act_*.py are designed to be run manually to perform some actions on media files
- qry_*.py are designed to be run manually to query some information from media files

## Verification scripts

## Action scripts

### do_inventory.py
- walks through all media folders and for each of them create inventory.json file which consist of
  - list of media files
  - checksum of each file
- media files are all folders without no_media.json file
