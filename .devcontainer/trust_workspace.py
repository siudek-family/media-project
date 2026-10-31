import json
import os
import tempfile
from pathlib import Path


def main() -> None:
    workspace = str(Path(__file__).resolve().parent.parent)
    config_dir = Path(os.environ.get("COPILOT_HOME") or Path.home() / ".copilot")
    config_path = config_dir.expanduser() / "config.json"
    header = ""
    config = {}
    if config_path.exists():
        lines = config_path.read_text(encoding="utf-8").splitlines(keepends=True)
        # Copilot prefixes its managed JSON with comment-only header lines.
        while lines and (not lines[0].strip() or lines[0].lstrip().startswith("//")):
            header += lines.pop(0)
        config = json.loads("".join(lines))
    if not isinstance(config, dict):
        raise ValueError(f"{config_path} must contain a JSON object")
    folders = config.setdefault("trustedFolders", [])
    if not isinstance(folders, list) or not all(
        isinstance(folder, str) for folder in folders
    ):
        raise ValueError(f"trustedFolders in {config_path} must be a list of paths")
    if workspace in folders:
        print(f"Copilot already trusts {workspace}")
        return
    folders.append(workspace)
    config_path.parent.mkdir(parents=True, exist_ok=True)
    with tempfile.NamedTemporaryFile(
        mode="w", encoding="utf-8", dir=config_path.parent, delete=False
    ) as temporary:
        temporary_path = Path(temporary.name)
        try:
            temporary.write(header + json.dumps(config, indent=2) + "\n")
            temporary.close()
            temporary_path.replace(config_path)
        finally:
            temporary_path.unlink(missing_ok=True)
    print(f"Copilot now trusts {workspace}")


if __name__ == "__main__":
    main()
