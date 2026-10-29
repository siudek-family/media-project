# Media Project

The project exposes media-library operations exclusively through local MCP servers. Each server and its tests live under `mcp/`; the media server reads and changes files only inside explicitly configured roots.

## Media MCP server

The current server provides tools to:

- list configured root identifiers, file metadata, and individual file details;
- calculate SHA-256 checksums;
- create per-file JSON inventory records in a separate catalog directory;
- rename files and move them between existing directories in the same root.

It does not read arbitrary file contents, delete or copy files, follow symbolic links/reparse points, create destination directories for move operations, or overwrite files. Directories containing `no_media.json` are excluded from inventory generation. Existing catalog records are preserved and reported as skipped. Rename and move tools execute immediately after validation; they do not have a separate confirmation step.

### Configure and start

1. Install Python 3.10+ and make its `python` command available to VS Code.
   Check it with `python --version`.
2. Edit `MEDIA_ROOTS` in `mcp/media_server/config.py`, adding only trusted,
   absolute media directories, for example:

   ```python
   MEDIA_ROOTS = {
       "family": Path(r"X:\Media\Family"),
   }
   ```

   The configuration intentionally starts empty, so filesystem tools fail
   closed until an operator configures roots. `CATALOG_ROOT` is currently
   `X:\OneDrive\Osobiste\Media\source`; change it to the desired absolute
   catalog path if appropriate. It must not overlap any configured media root.
3. Grant the Windows account running VS Code only the filesystem permissions
   required for the configured media and catalog directories. Application
   path validation is not a replacement for operating-system ACL isolation.
4. Start/restart `media-files` in VS Code. `.vscode/mcp.json` invokes
   `python mcp\start.py`. This standard-library launcher creates `mcp/.venv`
   if missing, installs the local package and its runtime dependencies from
   `mcp/pyproject.toml`, then starts the server over stdio. No manual `pip`
   command is needed for normal use.

Dependency installation is checked on every start; already satisfied packages
are reused without requesting upgrades. Installation may require internet access
and makes the first start slower. Installer output goes to stderr so stdout
remains reserved for MCP messages. If installation fails, the launcher reports
the error and does not start the server. Only runtime dependencies are installed;
test dependencies remain a separate development step. The MCP SDK is restricted
to version 1.x because this server uses its `FastMCP` API.

### Run tests

From the `mcp/` project directory, sync the development dependencies and run
the tests and type checker with `uv`:

```sh
uv sync --extra dev
uv run pytest
uv run mypy media_server
```

The Dev Container installs `uv` and runs `uv sync --extra dev` after creation.
The test suite uses temporary directories and does not require access to the
configured media collection.