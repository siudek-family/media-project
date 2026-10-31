"""Hardcoded allowlist configuration for the local media MCP server."""

from pathlib import Path

from media_server.filesystem import MediaFileService

# Media roots are the source folders the MCP tools can list, inspect, checksum,
# and create inventory records for. Each key is the root identifier passed to
# tools. Use only absolute, trusted paths.
# Leave empty to deny filesystem access.
MEDIA_ROOTS: dict[str, Path] = {
    "rodzinne": Path("ls /mnt/media/source/Rodzinne/"),
}

# Inventory output is kept separately from original media files.
CATALOG_ROOT = Path(r"X:\OneDrive\Osobiste\Media\source")

media_service = MediaFileService(MEDIA_ROOTS, CATALOG_ROOT)
