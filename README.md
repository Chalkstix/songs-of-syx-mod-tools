# SyxModTools

Tools and experiments around Songs of Syx mod compatibility.

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
class cooperatively.

## What this proves

`SyxAgentPoC` is a minimal [Java agent](https://docs.oracle.com/en/java/javase/21/docs/specs/jvm/se21/html/jvms-5.html)
(`-javaagent` JVM flag) that attaches to the Songs of Syx process and
registers a `ClassFileTransformer`. This is a standard JVM mechanism — it
requires no cooperation from the game, the mod SDK, or any mod author.

The PoC watches for any class whose name contains `SavedPrints` or
`UISavedPrints` and logs every load to `~/syx_agent_poc.log`, including the
raw bytecode size and which classloader loaded it.

Tested live against the real game (with the SDK + Better Blueprints mods
enabled): the agent attached cleanly, the game launched and ran normally,
and the log captured every single class load of `SavedPrints`,
`UISavedPrints`, and their inner classes as they happened — confirming the
exact conflict point is observable and, in principle, interceptable.

```
=== SyxAgentPoC premain() running. JVM started OK with -javaagent. ===
Working dir: C:\Steam\steamapps\common\Songs of Syx
Transformer registered. Watching for SavedPrints / UISavedPrints class loads...
INTERCEPTED CLASS LOAD: settlement/room/main/copy/SavedPrints | size=7714 bytes | loader=jdk.internal.loader.ClassLoaders$AppClassLoader@77be39eb
INTERCEPTED CLASS LOAD: view/sett/ui/room/prints/UISavedPrints | size=8496 bytes | loader=jdk.internal.loader.ClassLoaders$AppClassLoader@77be39eb
...
```

This PoC only **observes** — `transform()` returns `null`, leaving the
bytecode unchanged. The next step is a transformer that actually rewrites
the colliding classes (e.g. merging both mods' logic) instead of just
reporting on them. That's the basis for a future general mod-conflict
framework: detect collisions across a user's enabled mod set, and patch
them automatically instead of one mod silently losing.

## Files

- `SyxAgentPoC.java` — agent source
- `MANIFEST.MF` — jar manifest (`Premain-Class`, redefine/retransform capabilities)
- `SyxAgentPoC.jar` — built agent (see Build below)

## Build

Requires a JDK (tested with Microsoft Build of OpenJDK 21).

```
javac SyxAgentPoC.java
jar --create --file SyxAgentPoC.jar --manifest MANIFEST.MF SyxAgentPoC.class SyxAgentPoC$1.class
```

Note: the anonymous inner class (`SyxAgentPoC$1.class`, the
`ClassFileTransformer` implementation) must be included in the jar.
Leaving it out causes `premain()` to throw `NoClassDefFoundError`
uncaught, which silently aborts the entire JVM at startup before the game
ever runs — easy to mistake for a deeper compatibility problem.

## Usage

Add the agent to Songs of Syx's launcher settings
(`%appdata%/songsofsyx/settings/LauncherSettings.txt`, key `JVM_ARGS2`):

```
"-javaagent:C:/path/to/SyxModTools/SyxAgentPoC.jar",
```

Launch the game normally through the Songs of Syx launcher. Output appears
in `~/syx_agent_poc.log`.

## Status

Proof of concept only. Confirms the technical approach is viable; does not
yet fix any actual conflict.
