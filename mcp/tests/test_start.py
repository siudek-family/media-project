import os
import subprocess
import sys
from pathlib import Path
from unittest.mock import Mock

import pytest

import start


@pytest.fixture
def launcher(
    tmp_path: Path, monkeypatch: pytest.MonkeyPatch
) -> tuple[Path, Mock, Mock]:
    project = tmp_path / "project with spaces"
    project.mkdir()
    scripts = "Scripts" if os.name == "nt" else "bin"
    executable = "python.exe" if os.name == "nt" else "python"
    python = project / ".venv" / scripts / executable
    python.parent.mkdir(parents=True)
    python.touch()
    monkeypatch.setattr(start, "PROJECT_DIR", project)
    runner = Mock(return_value=subprocess.CompletedProcess([], 0))
    monkeypatch.setattr(start.subprocess, "run", runner)
    builder = Mock()
    monkeypatch.setattr(start.venv, "EnvBuilder", builder)
    return python, runner, builder


def test_installs_before_start_and_keeps_stdout_clean(launcher) -> None:
    python, runner, builder = launcher

    assert start.main() == 0
    builder.assert_not_called()
    install, server = runner.call_args_list
    assert install.args[0] == [
        str(python),
        "-m",
        "pip",
        "--disable-pip-version-check",
        "--no-input",
        "install",
        "-e",
        str(start.PROJECT_DIR),
    ]
    assert install.kwargs == {
        "cwd": start.PROJECT_DIR,
        "stdin": subprocess.DEVNULL,
        "stdout": sys.stderr,
        "stderr": sys.stderr,
        "check": True,
    }
    assert server.args[0] == [
        str(python), str(start.PROJECT_DIR / "server.py")
    ]
    assert server.kwargs == {"cwd": start.PROJECT_DIR}


def test_creates_environment_when_missing(launcher) -> None:
    python, _, builder = launcher
    python.unlink()

    assert start.main() == 0
    builder.assert_called_once_with(with_pip=True)
    builder.return_value.create.assert_called_once_with(
        start.PROJECT_DIR / ".venv"
    )


def test_install_failure_prevents_server_start(launcher, capsys) -> None:
    _, runner, _ = launcher
    runner.side_effect = subprocess.CalledProcessError(7, ["pip"])

    assert start.main() == 7
    assert runner.call_count == 1
    output = capsys.readouterr()
    assert output.out == ""
    assert "dependency installation failed" in output.err


def test_environment_failure_prevents_install(launcher, capsys) -> None:
    python, runner, builder = launcher
    python.unlink()
    builder.return_value.create.side_effect = OSError("Access denied")

    assert start.main() == 1
    runner.assert_not_called()
    assert "Access denied" in capsys.readouterr().err


def test_server_exit_code_is_preserved(launcher) -> None:
    _, runner, _ = launcher
    runner.side_effect = [
        subprocess.CompletedProcess([], 0),
        subprocess.CompletedProcess([], 9),
    ]

    assert start.main() == 9


def test_rejects_unsupported_python(launcher, monkeypatch, capsys) -> None:
    _, runner, builder = launcher
    monkeypatch.setattr(start.sys, "version_info", (3, 9))

    assert start.main() == 1
    runner.assert_not_called()
    builder.assert_not_called()
    assert "Python 3.10+" in capsys.readouterr().err
