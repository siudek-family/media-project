import json
import os
import shutil
import subprocess
import sys
from pathlib import Path

import pytest


@pytest.fixture
def setup_script(tmp_path: Path) -> Path:
    source = Path(__file__).resolve().parents[1] / ".devcontainer" / "trust_workspace.py"
    script = tmp_path / "another checkout" / ".devcontainer" / source.name
    script.parent.mkdir(parents=True)
    shutil.copyfile(source, script)
    return script


def run_setup(script: Path, config_dir: Path) -> subprocess.CompletedProcess[str]:
    return subprocess.run(
        [sys.executable, str(script)],
        env={**os.environ, "COPILOT_HOME": str(config_dir)},
        capture_output=True,
        text=True,
    )


def test_trusts_current_checkout_in_custom_copilot_home(
    setup_script: Path, tmp_path: Path
) -> None:
    config_dir = tmp_path / "copilot"

    result = run_setup(setup_script, config_dir)

    assert result.returncode == 0, result.stderr
    config = json.loads((config_dir / "config.json").read_text())
    assert config["trustedFolders"] == [str(setup_script.parent.parent)]


def test_preserves_existing_config_and_does_not_duplicate_trust(
    setup_script: Path, tmp_path: Path
) -> None:
    config_path = tmp_path / "config.json"
    header = "// User settings belong in settings.json.\n// Managed automatically.\n"
    existing = {"other": "https://example.com", "trustedFolders": ["/other/project"]}
    config_path.write_text(header + json.dumps(existing), encoding="utf-8")

    result = run_setup(setup_script, tmp_path)

    assert result.returncode == 0, result.stderr
    content = config_path.read_text()
    assert content.startswith(header)
    config = json.loads(content[len(header):])
    assert config == {
        **existing,
        "trustedFolders": ["/other/project", str(setup_script.parent.parent)],
    }

    result = run_setup(setup_script, tmp_path)

    assert result.returncode == 0, result.stderr
    assert config_path.read_text() == content


@pytest.mark.parametrize(
    "content",
    ["invalid json", "[]", '{"trustedFolders": "invalid"}', '{"trustedFolders": [42]}'],
)
def test_invalid_config_fails_without_overwriting(
    setup_script: Path, tmp_path: Path, content: str
) -> None:
    config_path = tmp_path / "config.json"
    config_path.write_text(content, encoding="utf-8")

    result = run_setup(setup_script, tmp_path)

    assert result.returncode != 0
    assert result.stderr
    assert config_path.read_text() == content
