---
name: version-bump
description: Bump the Liminal plugin version according to project semver rules. Run this whenever a session has produced shippable changes.
---

# version-bump — Liminal version rules

The plugin uses a 4-component version: `W.X.Y.Z` with a `Beta-v` prefix and an optional `-UNSTABLE` suffix. Stored in `gradle.properties` under `version=`.

| Slot | Bump when |
|------|-----------|
| **W** | Full release. **Reserved — currently 0.** Don't touch unless told. |
| **X** | A *bigger feature* — a new top-level system, a new player-facing flow, a new GUI hub, a new world layer, etc. |
| **Y** | A *system change* — modifying how an existing feature works, refactor, behavior change. |
| **Z** | Bug fixes, hotfixes, super small improvements, polish. |

## Hard rules

1. When a higher slot bumps, every slot to its right resets to 0:
   - `0.1.4.7` + bigger feature → `0.2.0.0` (not `0.2.4.7`)
   - `0.1.4.7` + system change → `0.1.5.0` (not `0.1.5.7`)
   - `0.1.4.7` + bug fix → `0.1.4.8`
2. `1.0.0.0` is **reserved for full release** — don't bump W until the user explicitly green-lights v1.
3. The `Beta-v` prefix stays on every version until the full release. Format is `Beta-v<W>.<X>.<Y>.<Z>` (with optional `-UNSTABLE` after).

## The `-UNSTABLE` suffix — when to add, when to drop

`-UNSTABLE` reflects **how destabilizing the project is *as a whole right now*** — not whether the latest commit was risky. It's a coarse "would I trust this build in production?" gauge.

**Add `-UNSTABLE` when** the change introduces a real risk the project didn't have before:
- Foundational rewrite (persistence layer, event-handling rewrite, world-data format change).
- New system that's likely to surface bugs only at runtime under load.
- Touching code paths that handle player save data or world generation in ways the existing tests don't cover.
- A change that, if it has a bug, could lose data or break compatibility for existing players or existing worlds.

**Don't add `-UNSTABLE` for** small/scoped work that doesn't widen the project's overall risk surface — typo fixes, GUI tweaks, single-feature additions in well-isolated subsystems, config defaults, dependency-free perf changes, etc.

**When you add `-UNSTABLE`, you must also write a project memory** at `memory/project_unstable_<short-name>.md` recording:
- Which area is unstable
- Why you think it's unstable (specific concern, not generic "it's new")
- What would have to be true to drop the suffix (e.g. "user confirms saves work", "audit pass with no critical findings")
- Add a one-line index entry to MEMORY.md

This way, future-you (or the user) can find the open concern by reading memory and decide when it's safe to drop.

**Drop `-UNSTABLE` when** the open concern(s) have been addressed:
- The pinning memory's "drop conditions" are met (user validated, audit passed, etc.) → delete the pinning memory and remove its MEMORY.md entry, then drop the suffix on the next bump.
- If multiple memories pin `-UNSTABLE`, only drop the suffix when the *last* one is resolved. Don't auto-drop just because one area got cleaned up; check for other open pins first.
- A focused audit of a pinned area that finds no critical bugs counts as validation for that pin — but only that pin.

**Don't:**
- Add `-UNSTABLE` reflexively to every bump.
- Drop `-UNSTABLE` without checking that all pinning memories are resolved.
- Keep `-UNSTABLE` on a build whose pinning memories are all resolved — that turns the flag into noise.

## What to do when invoked

1. Read `gradle.properties` to find the current version.
2. Decide which slot to bump based on the change just made:
   - New feature? → X
   - System change / refactor? → Y
   - Hotfix / small? → Z
3. **Write `.claude/COMMIT_NOTES.md`** describing what actually changed in this session — see [Commit message body](#commit-message-body) below. The build-and-push hook reads this file as the commit body and deletes it after a successful commit.
4. Rewrite the `version=` line in `gradle.properties` with the new version, resetting lower slots to 0.
5. **Decide on `-UNSTABLE`** (see the dedicated section above):
   - If the current version already has `-UNSTABLE`, check `memory/project_unstable_*.md` files. If their drop conditions are met, delete those memories + their MEMORY.md entries and drop the suffix. If any are still pinning, keep it.
   - If the current version is stable and your change introduces a new project-wide risk, add `-UNSTABLE` AND write a `project_unstable_<area>.md` memory documenting the concern + drop conditions, plus a MEMORY.md index line.
   - If neither, leave the suffix as-is.
6. The `build-and-push` hook (wired to PostToolUse on Edit|Write|MultiEdit, matching `gradle.properties`) will run `./gradlew build`, then `git add . && git commit && git push`. You don't have to run those manually.
7. Tell the user the old version → new version, what you bumped, and any `-UNSTABLE` add/drop with the reason.

## Commit message body

The build-and-push hook builds the commit message as:

```
<version>

<body from .claude/COMMIT_NOTES.md, or auto-generated path-group summary>
```

Always write `.claude/COMMIT_NOTES.md` before saving `gradle.properties` — the auto-generated fallback (path groups + add/modify/delete counts) is only there as a safety net when the LLM forgets. A human-readable summary is much better.

**What goes in the notes file** — keep it tight, oriented around behavior changes the user cares about, not file lists:

```
Add /flicker command (toggles light state in the player's current room; cosmetic for now, hooks for entity-spawn rules later).

Rewrite room-generation as zone-tagged chunk markers:
- Each generated chunk carries a Liminal "zone" PDC tag (level0, level1, ...).
- Room features query zone instead of biome — biome is no longer load-bearing.
- /zone shows the tag at the player's location; admins can override via /zone set <name>.

Migration: existing worlds without the PDC tag are auto-tagged level0 on first chunk load. No save format break.
```

- Lead with the user-visible change. File paths only when needed for migration / breakage notes.
- Mention any breaking change to data formats or configs in a separate paragraph.
- If `-UNSTABLE` was added, the body should mention why (mirrors the pinning memory).
- Don't repeat the version number in the body — it's already the title.

## What NOT to do

- Don't bump W without explicit user approval.
- Don't drop or change the `Beta-v` prefix.
- Don't bump multiple slots at once for one change.
- Don't keep `-UNSTABLE` on a version that's already been verified working.

## Example transitions

```
Beta-v0.0.0.1  →  +bug fix          →  Beta-v0.0.0.2
Beta-v0.0.0.2  →  +system change    →  Beta-v0.0.1.0
Beta-v0.0.1.0  →  +new GUI          →  Beta-v0.1.0.0
Beta-v0.1.0.0  →  +large refactor   →  Beta-v0.2.0.0-UNSTABLE
Beta-v0.2.0.0-UNSTABLE  →  validated working  →  Beta-v0.2.0.0
```
