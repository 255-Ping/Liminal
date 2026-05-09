---
name: text-style
description: Liminal text formatting conventions. Apply whenever you write or edit any user-facing text in this plugin — chat messages, GUI titles, item display names, item lore, command feedback. The single source of truth for colors, decorations, and the chat prefix. Backrooms-themed palette.
---

# Liminal text style

All user-facing text uses Adventure `Component`. Never use legacy `§` codes, `ChatColor`, or `String`-based names/lore. Every Component you build for an item name or lore line must have `.decoration(TextDecoration.ITALIC, false)` applied — Minecraft otherwise italicizes any renamed item.

The palette is **backrooms-coded**: muted, fluorescent, clinical. Yellow walls, gray carpet, buzzing gold light, occasional blood-red warning signage. Don't introduce new colors without updating this skill.

## Chat messages

Every chat message starts with the prefix, then the body. Route through a single `Messages` util in `com.github.ping.liminal.util` — that way an admin's `formatting.yml` (prefix toggle, text, colors) takes effect. **Don't** create new `private static final Component PREFIX = ...` constants per file; use the runtime accessor at every call site.

Expected shape (build this util early, before adding any feature that talks to players):

```java
import com.github.ping.liminal.util.Messages;
player.sendMessage(Messages.prefix().append(Component.text("Door sealed.", Messages.success())));
```

Body color by intent — name accessors so the call site reads as intent, not as color:

| Kind | Accessor | Default |
|---|---|---|
| Error / refusal | `Messages.error()` | `RED` |
| Success | `Messages.success()` | `GREEN` |
| Info / progress / neutral | `Messages.info()` | `GRAY` |
| Highlighted value embedded in body (numbers, coords, names, durations) | `Messages.highlight()` | `YELLOW` |

Highlighted values appear inline by `.append(...)`-ing a `YELLOW` Component into the body — do not recolor the surrounding body.

## GUI titles

`GOLD` + `BOLD`. Title only — no prefix in GUI titles. Reads as a buzzing fluorescent sign above a door.

## GUI item display names

| Role | Color |
|---|---|
| Primary action (confirm, start, the main thing the GUI does) | `YELLOW` + `BOLD` |
| Cancel / destructive / close | `RED` + `BOLD` |
| Disabled / on-cooldown / unavailable | `RED` + `BOLD` (swap the icon to a `CLOCK` or similar to signal state) |
| Decorative filler (glass panes) | single space `" "`, no color |

`YELLOW` is the iconic backrooms wall color — it's the cue that says "this is the thing to click." Reserve it for the primary call-to-action; don't bleed it onto secondary buttons.

## Hiding vanilla tooltip extras on GUI items

Decorative GUI items (summary tiles, labels, nav buttons, fillers — anything we built for display, not the player's real item) must not leak vanilla tooltip lines like `When in Main Hand: 4 Attack Damage`, enchant glints from icon fakery, trim names, or dye color. Build a helper at `com.github.ping.liminal.gui.GuiItems.hideExtras(meta)` that sets an empty `AttributeModifiers` component and applies all `ItemFlag` values; call it on every decorative meta before `setItemMeta`.

Do **not** apply `hideExtras` to a snapshot of the player's actual item being displayed for inspection — that one should keep its real attributes and enchants visible.

## GUI item lore

| Role | Color |
|---|---|
| Description text | `GRAY` |
| Highlighted value inside a lore line (numbers, durations, coords) | `YELLOW` |
| Tertiary metadata (cooldown labels, requirements, footnotes) | `DARK_GRAY` |
| Blank spacer line | `Component.empty()` |

Highlighted values inside lore use the same inline-`.append(...)` pattern as chat messages.

## Quick reference: palette in use

- `GOLD` — GUI titles (buzzing fluorescent overhead light)
- `YELLOW` — primary action buttons, highlighted values (iconic walls)
- `RED` — errors, cancel, disabled state (warning signage / blood)
- `GREEN` — success
- `GRAY` — info, descriptions (carpet, concrete)
- `DARK_GRAY` — chat prefix brackets, tertiary lore metadata (shadows, mildew)

Do not introduce new colors without updating this skill.

## Backrooms voice notes

When you write copy, lean into the aesthetic. The plugin is a survival overhaul of vanilla into the backrooms — error messages, item lore, and GUI titles should sound clinical and slightly off:

- Prefer terse, declarative copy. "Door sealed." not "You have successfully sealed the door!"
- Avoid exclamation points and cheerful emoji-equivalents. The vibe is fluorescent buzz, not party.
- Item lore can hint at state without explaining mechanics — "Hums faintly" reads better than "+5 attack damage" for a cursed-feeling item.
- Don't break the fiction with developer voice ("debug:", "TODO:", admin command output is fine plain).

These are guidelines, not hard rules — readability beats atmosphere when the player needs to act on the message.
