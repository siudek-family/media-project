import json
from types import SimpleNamespace

import pytest

from media_server import server
from media_server.filesystem import MediaConfigurationError


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


def test_root_listing_does_not_disclose_paths(
    monkeypatch: pytest.MonkeyPatch,
) -> None:
    monkeypatch.setattr(
        server,
        "media_service",
        SimpleNamespace(configured_roots=lambda: [{"root_id": "family"}]),
    )

    assert json.loads(server.list_configured_roots()) == [
        {"root_id": "family"}
    ]


def test_main_reports_configuration_failure_without_traceback(
    monkeypatch: pytest.MonkeyPatch, capsys: pytest.CaptureFixture[str]
) -> None:
    def fail_to_load() -> None:
        raise MediaConfigurationError(
            "Configured catalog directory is unavailable: "
            "No such file or directory: '/media/catalog'"
        )

    monkeypatch.setattr(server, "load_media_service", fail_to_load)
    monkeypatch.setattr(
        server.mcp, "run", lambda: pytest.fail("Server should not start.")
    )

    with pytest.raises(SystemExit) as exc_info:
        server.main()

    captured = capsys.readouterr()
    assert exc_info.value.code == 1
    assert captured.out == ""
    assert captured.err == (
        "Media MCP server failed to start: "
        "Configured catalog directory is unavailable: "
        "No such file or directory: '/media/catalog'\n"
    )
    assert "Traceback" not in captured.err
