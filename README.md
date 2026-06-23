# SyxModTools

Tools for detecting and (eventually) resolving compatibility conflicts
between Songs of Syx mods.

## Background

Songs of Syx mods can ship compiled `.class` files that override the game's
own classes. If two mods happen to define a class with the same fully
qualified name, only one definition actually loads — the other is silently
dropped, with no warning or error anywhere. Whichever mod's class "wins"
depends on load order, and the loser's feature just silently doesn't work.

This is a real, reproducible conflict between two published Workshop mods:

- **More Options** (by 4rg0n and Senso)
- **Better Blueprints** (by Nerond)

Both ship their own version of `settlement/room/main/copy/SavedPrints` and
`view/sett/ui/room/prints/UISavedPrints`. With both mods enabled, More
Options' version wins, so Better Blueprints' blueprint UI never appears in
game — even though the mod is active.

The game's own modding support (see `info/MAKE_A_MOD.txt` in the install
directory) only documents whole-class replacement. There's no official
hook/patch mechanism that would let two mods both extend or modify the same
class cooperatively, and no built-in way to even find out a collision is
happening short of noticing a feature silently missing.

## mod-conflict-detector

A console tool that statically scans your enabled mods' jars and reports
any class name defined by more than one of them — no need to launch the
game.

### Build

Requires a JDK (tested with JDK 21) and Maven.

```
mvn package
```

Produces `target/mod-conflict-detector.jar`.

### Run

```
java -jar target/mod-conflict-detector.jar --game-dir "C:\Steam\steamapps\common\Songs of Syx"
```

The Workshop content folder is derived from `--game-dir` assuming the
standard Steam layout (`<library>/steamapps/common/<game>` and
`<library>/steamapps/workshop/content/<appid>`). The enabled mod list and
load order are read from `LauncherSettings.txt`
(`%APPDATA%/songsofsyx/settings/LauncherSettings.txt` by default).

Optional flags:

- `--launcher-settings <path>` — override the LauncherSettings.txt location
- `--workshop-dir <path>` — override the derived Workshop content folder directly

Example output:

```
Reading enabled mods from C:\Users\you\AppData\Roaming\songsofsyx\settings\LauncherSettings.txt ...
Found 3 enabled mod id(s).

Scanned Mod SDK V0 -> 1505 class(es)
Scanned More Options -> 842 class(es)
Scanned Better Blueprints -> 131 class(es)

Found 1 class-name collision(s):

  settlement.room.main.copy.SavedPrints
    defined by (load order): More Options, Better Blueprints
    -> only one definition can load; the other mod's version of this class is silently ignored.

Load order above follows your MODS list in LauncherSettings.txt. Which mod
actually wins isn't guaranteed by this tool alone -- if a feature seems
missing in-game, try changing mod load order or check with the mod authors.
```

### Status

Detection only. Does not yet attempt to fix a collision automatically —
that's the planned next step, building on the `agent-poc/` proof of concept
below.

## agent-poc

`agent-poc/` holds a small experiment that answers a narrower question:
*is a hook/patch-style fix even technically possible* in Songs of Syx, as
opposed to the whole-class-replacement override the game officially
supports?

`SyxAgentPoC` is a minimal [Java agent](https://docs.oracle.com/en/java/javase/21/docs/specs/jvm/se21/html/jvms-5.html)
(`-javaagent` JVM flag) that attaches to the Songs of Syx process and
registers a `ClassFileTransformer`, watching for `SavedPrints`/
`UISavedPrints` class loads and logging them. This is a standard JVM
mechanism — it requires no cooperation from the game, the mod SDK, or any
mod author.

Tested live against the real game (with the SDK + Better Blueprints mods
enabled): the agent attached cleanly, the game launched and ran normally,
and the log captured every single class load of `SavedPrints`,
`UISavedPrints`, and their inner classes as they happened — confirming the
exact conflict point is observable and, in principle, rewritable.

```
=== SyxAgentPoC premain() running. JVM started OK with -javaagent. ===
Transformer registered. Watching for SavedPrints / UISavedPrints class loads...
INTERCEPTED CLASS LOAD: settlement/room/main/copy/SavedPrints | size=7714 bytes | loader=jdk.internal.loader.ClassLoaders$AppClassLoader@77be39eb
INTERCEPTED CLASS LOAD: view/sett/ui/room/prints/UISavedPrints | size=8496 bytes | loader=jdk.internal.loader.ClassLoaders$AppClassLoader@77be39eb
...
```

This PoC only **observes** — `transform()` returns `null`, leaving the
bytecode unchanged. A future version would rewrite the colliding classes
(e.g. merging both mods' logic) instead of just reporting on them — that's
the real fix this whole project is working toward.

Build:

```
cd agent-poc
javac SyxAgentPoC.java
jar --create --file SyxAgentPoC.jar --manifest MANIFEST.MF SyxAgentPoC.class SyxAgentPoC$1.class
```

Note: the anonymous inner class (`SyxAgentPoC$1.class`, the
`ClassFileTransformer` implementation) must be included in the jar.
Leaving it out causes `premain()` to throw `NoClassDefFoundError`
uncaught, which silently aborts the entire JVM at startup before the game
ever runs — easy to mistake for a deeper compatibility problem.

Usage: add to `JVM_ARGS2` in `LauncherSettings.txt`:

```
"-javaagent:C:/path/to/SyxModTools/agent-poc/SyxAgentPoC.jar",
```

Output appears in `~/syx_agent_poc.log`.

## Design principles

For anyone contributing:

- **Static analysis first.** Prefer inspecting mod jars on disk over
  requiring the game to be launched. It's faster, safer, and works in CI;
  reserve runtime techniques (like the javaagent) for things that
  genuinely can't be determined statically.
- **Never run silently.** Any tool here that takes more than an instant
  should print what it's doing as it goes (which mod it's scanning, etc.),
  not just a final result. Users should never wonder if it's frozen.
- **No surprises for the user's game install.** Tools read from the game
  and mod folders; they should not modify `LauncherSettings.txt`, mod
  files, or save data unless that's the tool's explicit, stated purpose.
- **Keep output plain and actionable.** Reports should say what's wrong
  and where (class name, which mods), not just "conflict detected."

## Status

Detector v1 (static class-collision scan) is working. Auto-resolution of
detected conflicts is the next major piece of work.
