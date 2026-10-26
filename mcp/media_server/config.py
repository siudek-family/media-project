"""Hardcoded allowlist configuration for the local media MCP server."""

from pathlib import Path

from media_server.filesystem import MediaFileService

# Add only absolute, trusted directories. Leave empty to deny filesystem access.
MEDIA_ROOTS: dict[str, Path] = {}

# Inventory output is kept separately from original media files.
CATALOG_ROOT = Path(r"X:\OneDrive\Osobiste\Media\source")

media_service = MediaFileService(MEDIA_ROOTS, CATALOG_ROOT)
