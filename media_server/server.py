"""MCP tools for a locally configured media collection."""

import json

from mcp.server.fastmcp import FastMCP

from media_server.config import load_media_service
from media_server.filesystem import MediaConfigurationError, MediaFileService

mcp = FastMCP("media-files")
media_service: MediaFileService | None = None


def _get_media_service() -> MediaFileService:
    if media_service is None:
        raise MediaConfigurationError("Media server configuration is not loaded.")
    return media_service


@mcp.tool()
def list_configured_roots() -> str:
    """List configured media-root identifiers without revealing host paths."""
    return json.dumps(_get_media_service().configured_roots())


@mcp.tool()
def list_media_files(root_id: str, directory: str = ".") -> str:
    """List regular files and basic metadata in a configured directory."""
    return json.dumps(
        _get_media_service().list_media_files(root_id, directory),
        ensure_ascii=False,
    )


@mcp.tool()
def get_file_info(root_id: str, relative_path: str) -> str:
    """Read basic metadata for one regular media file."""
    return json.dumps(
        _get_media_service().get_file_info(root_id, relative_path),
        ensure_ascii=False,
    )


@mcp.tool()
def compute_checksum(root_id: str, relative_path: str) -> str:
    """Calculate the SHA-256 checksum of one media file."""
    return _get_media_service().compute_checksum(root_id, relative_path)


@mcp.tool()
def create_inventory(root_id: str) -> str:
    """Create missing per-file JSON inventory records in the catalog."""
    return json.dumps(_get_media_service().create_inventory(root_id))


@mcp.tool()
def rename_media_file(
    root_id: str, source_path: str, destination_path: str
) -> str:
    """Rename one regular file within its existing directory."""
    return _get_media_service().rename_media_file(
        root_id, source_path, destination_path
    )


@mcp.tool()
def move_media_file(
    root_id: str, source_path: str, destination_path: str
) -> str:
    """Move one regular file to another existing directory in the same root."""
    return _get_media_service().move_media_file(
        root_id, source_path, destination_path
    )


def main() -> None:
    """Start the MCP server over stdio."""
    global media_service
    media_service = load_media_service()
    mcp.run()
