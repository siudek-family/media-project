import json

from media_server import server


def test_server_registers_expected_tools() -> None:
    tool_names = {tool.name for tool in server.mcp._tool_manager.list_tools()}

    assert tool_names == {
        "list_configured_roots",
        "list_media_files",
        "get_file_info",
        "compute_checksum",
        "create_inventory",
        "rename_media_file",
        "move_media_file",
    }


def test_root_listing_does_not_disclose_paths(monkeypatch) -> None:
    monkeypatch.setattr(
        server.media_service,
        "configured_roots",
        lambda: [{"root_id": "family"}],
    )

    assert json.loads(server.list_configured_roots()) == [
        {"root_id": "family"}
    ]
