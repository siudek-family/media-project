# Media Project

The project exposes media-library operations exclusively through local MCP servers. The server and its tests live at the repository root; the media server reads and changes files only inside explicitly configured roots.

## Media MCP server

The current server provides tools to:

- list configured root identifiers, file metadata, and individual file details;
- calculate SHA-256 checksums;
- create per-file JSON inventory records in a separate catalog directory;
- rename files and move them between existing directories in the same root.

It does not read arbitrary file contents, delete or copy files, follow symbolic links/reparse points, create destination directories for move operations, or overwrite files. Directories containing `no_media.json` are excluded from inventory generation. Existing catalog records are preserved and reported as skipped. Rename and move tools execute immediately after validation; they do not have a separate confirmation step.

### Configure and start

1. Install [uv](https://docs.astral.sh/uv/getting-started/installation/) and
   make its `uv` command available to VS Code. Check it with `uv --version`.
   `uv` uses a compatible Python 3.10+ interpreter, downloading one if needed.
2. Edit `media_server/config.json`, adding only trusted, absolute media
   directories and an existing catalog directory, for example:

   ```json
   {
     "media_roots": {
       "family": "/mnt/media/Family"
     },
     "catalog_root": "/mnt/media/catalog"
   }
   ```

   The server validates at startup that every configured path is absolute,
   exists, and is a directory. It exits with an error if a directory is
   missing or if the catalog overlaps a media root. Create the catalog
   directory before starting the server.
3. Grant the Windows account running VS Code only the filesystem permissions
   required for the configured media and catalog directories. Application
   path validation is not a replacement for operating-system ACL isolation.
4. Start/restart `media-files` in VS Code. `.vscode/mcp.json` invokes
   `uv run --locked media-mcp` with the repository root as the working directory. `uv`
   creates `.venv` if missing, synchronizes the local package and its runtime
   dependencies from `uv.lock`, then starts the server over stdio. No manual
   environment setup or `pip` command is needed for normal use.

The environment is checked on every start; synchronized packages are reused
without requesting upgrades. Installation may require internet access and makes
the first start slower. `uv` writes setup output to stderr so stdout remains
reserved for MCP messages. If setup fails or the lockfile is out of date, `uv`
reports the error and does not start the server. Only runtime dependencies are installed;
test dependencies remain a separate development step. The MCP SDK is restricted
to version 1.x because this server uses its `FastMCP` API.

### Run tests

From the repository root, sync the development dependencies and run
the tests and type checker with `uv`:

```sh
uv sync --extra dev
uv run pytest
uv run mypy media_server
```

The Dev Container installs `uv`; run the commands above to set up development dependencies.
The test suite uses temporary directories and does not require access to the
configured media collection.