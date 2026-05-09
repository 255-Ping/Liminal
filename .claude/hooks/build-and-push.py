#!/usr/bin/env python3
"""
PostToolUse hook for Edit|Write|MultiEdit. Triggers whenever a file is edited.

If the edited file ends with `/gradle.properties`:
  1. Runs `./gradlew build`. If the build fails, stops here — we don't ship broken code.
  2. Reads the new version= line from gradle.properties.
  3. Builds the commit message:
       - Title: "<version>"
       - Body: contents of `.claude/COMMIT_NOTES.md` if present (LLM-written summary),
         otherwise an auto-generated grouping of changed paths from `git status --porcelain`.
       The notes file is deleted after a successful commit so the next run starts clean.
  4. `git add .` + `git commit -F <tmpfile>` (skips push silently if nothing to commit —
     happens if gradle.properties is touched without a real change, e.g. whitespace).
  5. `git push`. If push fails (network, auth, non-fast-forward, etc.), prints the error
     so the user can recover manually; the local commit is still on the branch.

Anything other than gradle.properties: exits 0 immediately, no work.
"""
import json, os, re, subprocess, sys, tempfile
from collections import defaultdict


def run(cmd, cwd=None, capture=False):
    """Run a subprocess command. Returns CompletedProcess. Doesn't raise on non-zero."""
    return subprocess.run(cmd, cwd=cwd, capture_output=capture, text=True)


# Map a path to a coarse grouping label for the auto-summary fallback.
GROUP_PATTERNS = [
    (re.compile(r"^src/main/resources/plugin\.yml$"),     "plugin manifest"),
    (re.compile(r"^src/main/resources/config\.yml$"),     "main config"),
    (re.compile(r"^src/main/resources/"),                 "resources"),
    (re.compile(r"^src/main/java/.+/command/"),           "commands"),
    (re.compile(r"^src/main/java/.+/gui/"),               "GUIs"),
    (re.compile(r"^src/main/java/.+/listener/"),          "listeners"),
    (re.compile(r"^src/main/java/.+/util/"),              "util"),
    (re.compile(r"^src/main/java/.+/Liminal\.java$"),     "plugin wiring"),
    (re.compile(r"^src/main/java/.+/([^/]+)/[^/]+\.java$"),  None),  # take subdir
    (re.compile(r"^docs/"),                               "docs"),
    (re.compile(r"^README\.md$"),                         "README"),
    (re.compile(r"^CLAUDE\.md$"),                         "CLAUDE.md"),
    (re.compile(r"^gradle\.properties$"),                 "version"),
    (re.compile(r"^build\.gradle"),                       "build script"),
    (re.compile(r"^\.claude/"),                           "harness"),
]


def classify(path: str) -> str:
    for pattern, label in GROUP_PATTERNS:
        m = pattern.match(path)
        if m:
            return label if label is not None else m.group(1)
    # Fall back to the first path segment.
    return path.split("/", 1)[0] or "misc"


def auto_summary() -> str:
    """Build a short body describing what changed, grouped by area. Empty string on failure."""
    out = run(["git", "diff", "--cached", "--name-status"], capture=True)
    if out.returncode != 0 or not out.stdout.strip():
        return ""
    groups = defaultdict(lambda: {"A": 0, "M": 0, "D": 0, "R": 0})
    for line in out.stdout.splitlines():
        parts = line.split("\t")
        if not parts:
            continue
        status = parts[0][0]  # A/M/D/R + score for renames
        path = parts[-1]      # last column is the post-rename path
        label = classify(path)
        if status not in ("A", "M", "D", "R"):
            status = "M"
        groups[label][status] += 1
    if not groups:
        return ""
    lines = []
    for label in sorted(groups.keys()):
        stats = groups[label]
        bits = []
        if stats["A"]: bits.append(f"+{stats['A']}")
        if stats["M"]: bits.append(f"~{stats['M']}")
        if stats["D"]: bits.append(f"-{stats['D']}")
        if stats["R"]: bits.append(f"r{stats['R']}")
        lines.append(f"- {label}: {' '.join(bits)}")
    return "Changes:\n" + "\n".join(lines)


def main():
    try:
        payload = json.load(sys.stdin)
    except Exception:
        return 0  # No JSON → not a real hook invocation. Bail.

    fp = payload.get("tool_input", {}).get("file_path", "")
    if not fp.endswith("/gradle.properties"):
        return 0

    project = os.path.dirname(fp)
    os.chdir(project)

    # 1. Build. Halt the chain if the build fails — don't ship broken code.
    build = run(["./gradlew", "build"])
    if build.returncode != 0:
        print(f"build failed (rc={build.returncode}); skipping commit + push", file=sys.stderr)
        return build.returncode

    # 2. Read the new version.
    version = "unknown"
    try:
        with open("gradle.properties") as f:
            m = re.search(r"^version=(.+)$", f.read(), re.MULTILINE)
            if m:
                version = m.group(1).strip()
    except OSError:
        pass

    # 3. Stage everything so the body summary reflects the full commit.
    run(["git", "add", "."])

    # 4. Build commit message: <version> as title; body from COMMIT_NOTES.md if present,
    # otherwise an auto-generated path-group summary.
    notes_path = ".claude/COMMIT_NOTES.md"
    body = ""
    if os.path.exists(notes_path):
        try:
            with open(notes_path) as f:
                body = f.read().strip()
        except OSError:
            pass
    if not body:
        body = auto_summary()

    msg = version if not body else f"{version}\n\n{body}\n"
    with tempfile.NamedTemporaryFile("w", suffix=".msg", delete=False) as tf:
        tf.write(msg)
        msg_file = tf.name
    try:
        commit = run(["git", "commit", "-F", msg_file], capture=True)
    finally:
        try: os.unlink(msg_file)
        except OSError: pass

    if commit.returncode != 0:
        # Most common cause: nothing to commit. That's fine — gradle.properties was touched
        # without a real change (whitespace edit, save reorder, etc.). Don't push.
        if "nothing to commit" in (commit.stdout or "") + (commit.stderr or ""):
            return 0
        print(f"git commit failed: {commit.stderr or commit.stdout}", file=sys.stderr)
        return commit.returncode

    # Commit succeeded — clear the notes file so the next bump starts clean.
    if os.path.exists(notes_path):
        try: os.unlink(notes_path)
        except OSError: pass

    # 5. Push.
    push = run(["git", "push"], capture=True)
    if push.returncode != 0:
        print(f"git push failed (rc={push.returncode}): {push.stderr or push.stdout}",
              file=sys.stderr)
        print("Local commit is on the branch; resolve and push manually.", file=sys.stderr)
        return push.returncode

    print(f"Pushed {version} to origin.", file=sys.stderr)
    return 0


if __name__ == "__main__":
    sys.exit(main())
