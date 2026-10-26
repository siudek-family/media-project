"""Self-installing launcher for the media MCP server.

Why this exists:
    VS Code starts MCP servers itself, with no chance to run setup steps
    first. If the server's interpreter lacks the ``mcp`` package, the process
    exits before the MCP handshake and VS Code only reports a failed start.
    Requiring a manual ``pip install`` before first use (and after every
    dependency change) is easy to forget, so this launcher makes the server
    start reliably from a fresh checkout.

Why it is a separate script:
    It must run on the system ``python`` using only the standard library,
    because the virtual environment and its packages may not exist yet.

Why installer output goes to stderr:
    For stdio servers, stdout is the MCP protocol channel. Any pip output
    written there would corrupt the handshake.
"""

import os
import subprocess
import sys
import venv
from pathlib import Path

PROJECT_DIR = Path(__file__).resolve().parent


def main() -> int:
    """Install runtime dependencies, then start MCP with clean protocol stdout."""
    if sys.version_info < (3, 10):
        print("The media MCP server requires Python 3.10+.", file=sys.stderr)
        return 1

    environment = PROJECT_DIR / ".venv"
    scripts = "Scripts" if os.name == "nt" else "bin"
    executable = "python.exe" if os.name == "nt" else "python"
    python = environment / scripts / executable

    try:
        if not python.is_file():
            print("Creating the local MCP environment...", file=sys.stderr)
            venv.EnvBuilder(with_pip=True).create(environment)

        print("Installing/checking MCP runtime dependencies...", file=sys.stderr)
        subprocess.run(
            [
                str(python),
                "-m",
                "pip",
                "--disable-pip-version-check",
                "--no-input",
                "install",
                "-e",
                str(PROJECT_DIR),
            ],
            cwd=PROJECT_DIR,
            stdin=subprocess.DEVNULL,
            stdout=sys.stderr,
            stderr=sys.stderr,
            check=True,
        )
        return subprocess.run(
            [str(python), str(PROJECT_DIR / "server.py")],
            cwd=PROJECT_DIR,
        ).returncode
    except subprocess.CalledProcessError as error:
        print(
            f"MCP dependency installation failed (exit code {error.returncode}). "
            "The server was not started; see installer output above.",
            file=sys.stderr,
        )
        return error.returncode
    except OSError as error:
        print(f"Cannot prepare or start the MCP server: {error}", file=sys.stderr)
        return 1


if __name__ == "__main__":
    sys.exit(main())
