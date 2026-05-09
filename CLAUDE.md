# Liminal

A Paper plugin that overhauls vanilla Minecraft into a backrooms survival experience. Early-stage — the source tree is mostly skeleton, conventions below are aspirational and meant to guide what gets built.

- **Package root:** `com.github.ping.liminal`
- **Plugin class:** `com.github.ping.liminal.Liminal`
- **Build:** `./gradlew build` — produces a jar in `build/libs/`
- **Paper API:** `26.1.2` (toolchain Java 25; `runServer` task uses Minecraft `26.1.2`)

## Skills you must use

The `.claude/skills/` directory contains the durable rules for working in this repo. Use them — don't paraphrase from memory and don't reinvent conventions.

- **`text-style`** — apply *every time* you write or edit user-facing text (chat messages, GUI titles, item display names, item lore, command feedback). The single source of truth for the backrooms-themed palette and the `.decoration(ITALIC, false)` rule. If you're touching a `Component`, this skill is in scope.
- **`version-bump`** — invoke this when a session has produced shippable changes and the user is ready to ship. It owns the 4-component `Beta-vW.X.Y.Z` scheme, the `-UNSTABLE` suffix logic, and the commit-notes file. Never hand-edit `gradle.properties` for a release.
- **`ultimate-protocol`** — only when the user explicitly requests it ("ultimate mode", "json mode", etc.). Forces JSON-only output. Don't volunteer it.

## Hook: build-and-push

`.claude/hooks/build-and-push.py` is wired as a PostToolUse hook on `Edit|Write|MultiEdit`. It only fires for edits to `gradle.properties`:

1. Runs `./gradlew build` — fails the chain on build error.
2. Stages everything, commits with title `<version>` and body from `.claude/COMMIT_NOTES.md` (LLM-written) or an auto-generated path-group summary.
3. Pushes to `origin`.

**Implication:** the only path to a commit is through a `gradle.properties` save. That means a version bump *is* a release. The `version-bump` skill writes `.claude/COMMIT_NOTES.md` first, then bumps the version — which fires the hook and ships the work.

If you want to commit without bumping the version, do it manually with `git`. Don't touch `gradle.properties` to "force a commit."

## Conventions to honor when building features

These are forward-looking — most don't exist in the source yet because the project is new. When you build the first instance, build it this way:

- **Feature toggles.** New player-facing features land behind a `features.<name>` boolean in `src/main/resources/config.yml`, with a guarded init block in `Liminal.onEnable`. Don't wire features unconditionally.
- **Per-feature subfolders.** Configs and data for a feature live under `src/main/resources/<feature>/`, not flat at the resources root.
- **Feature layout.** Split per feature into `command/`, `listener/`, `gui/`, `manager/` packages under `com.github.ping.liminal.<feature>.*`.
- **Commands.** Every command class implements `CommandExecutor + TabCompleter`. Register both `setExecutor` and `setTabCompleter` in `Liminal.onEnable`. Empty completions return `Collections.emptyList()`.
- **Chat utility.** Build `com.github.ping.liminal.util.Messages` early — `prefix()`, `error()`, `success()`, `info()`, `highlight()` accessors. All chat call sites route through it. No per-file `private static final Component PREFIX = ...` constants. See `text-style` skill.
- **GUI utility.** Build `com.github.ping.liminal.gui.GuiItems.hideExtras(meta)` early — sets empty `AttributeModifiers` and applies all `ItemFlag`s. Call it on every decorative meta before `setItemMeta`. Don't apply it to a snapshot of the player's real item used for inspection.
- **Voice.** Backrooms-coded copy: terse, declarative, slightly clinical. No exclamation points, no cheerful flourishes. "Door sealed." not "You sealed the door!"

## What not to do

- Don't introduce legacy `§` color codes or `ChatColor`. Adventure `Component` only.
- Don't add new colors to the palette without updating the `text-style` skill.
- Don't bump the major version slot (`W`) without explicit user approval — `1.0.0.0` is reserved for full release.
- Don't drop the `Beta-v` prefix or change its format.
- Don't auto-amend commits; the hook always creates new commits.
