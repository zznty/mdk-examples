# Jar-in-Jar examples (cross-loader)

These examples embed a plain Java library (`greetlib`) **inside** the mod jar — "Jar-in-Jar" (JiJ) — so the
mod can use it at runtime without the user installing it separately. The same library is embedded across
**Forge, NeoForge and Fabric**, using a single plugin: a cross-loader fork of Forge's JarJar, published as
**`net.zznty.jarjar`** on [`maven.zznty.ru`](https://maven.zznty.ru).

| Example | Loader | MC | What it shows |
|---------|--------|----|----|
| [`forge-1.21.1`](forge-1.21.1) | Forge | 1.21.1 | `loader = 'forge'` |
| [`neoforge-26.1.2`](neoforge-26.1.2) | NeoForge | 26.1.2 | `loader = 'neoforge'` (same format as Forge) |
| [`fabric-26.1.2`](fabric-26.1.2) | Fabric | 26.1.2 | `loader = 'fabric'` (the special case) |
| [`multiloader-1.21.1`](multiloader-1.21.1) | Forge + Fabric | 1.21.1 | one shared lib, `loader = project.name` |

## Why a loader matters for JiJ

The *physical* nesting (a jar copied inside another jar) is trivial. The hard part is that each modloader only
puts a nested jar on the classpath if it carries a **loader-specific marker** — otherwise the embedded classes
exist on disk but throw `ClassNotFoundException` at runtime. The plugin's `loader` property handles this:

| Loader | How the nested jar is referenced | What a plain library needs to be loaded |
|--------|----------------------------------|------------------------------------------|
| Forge / NeoForge | `META-INF/jarjar/metadata.json` (identical format on both) | `FMLModType: LIBRARY` in the nested jar's `MANIFEST.MF` |
| Fabric | parent mod's `fabric.mod.json` → `"jars": [...]` | the nested jar must itself be a Fabric mod (own `fabric.mod.json`) |

So for **Forge/NeoForge** the plugin writes `metadata.json` and stamps `FMLModType: LIBRARY` +
`Automatic-Module-Name` onto any embedded jar that isn't already a mod. For **Fabric** it skips `metadata.json`
entirely, patches *your* `fabric.mod.json` to list the nested jar, and synthesizes a minimal `fabric.mod.json`
*inside* the embedded library so Fabric treats it as a nested mod.

## Usage in a nutshell

`settings.gradle` — the plugin lives on our maven:

```groovy
pluginManagement {
    repositories {
        maven { url = 'https://maven.zznty.ru/releases' }
        gradlePluginPortal()
        mavenCentral()
    }
}
```

`build.gradle`:

```groovy
plugins {
    id 'net.zznty.forgegradle' version '[7.0.29,8.0)'
    id 'net.zznty.jarjar'      version '[0.2,1.0)'
}

// Create the jarJar task and tell it which loader the output targets:
//   'forge' (default) | 'neoforge' | 'fabric'
jarJar.register {
    loader = 'fabric'
}

dependencies {
    // compileOnly: compile against the lib. jarJar(...): embed + make it runtime-loadable in the shipped jar.
    compileOnly project(':greetlib')
    jarJar(project(':greetlib')) {
        jarJar.configure(it) { version = '1.0.0' }
    }
}
```

The task produces **`<archivesName>-all.jar`** — that is the jar you ship. Inspect any example's output to see
the loader-specific structure described above.

## The shared `greetlib` library

Every example includes a tiny `greetlib` subproject — an ordinary Java library with **no Minecraft and no
loader dependency**. It exposes a single `Greeter.greet(String)` method; each mod logs its result on startup.
If JiJ is wired correctly, that call resolves at runtime even though `greetlib` was never installed separately.

## Notes

- The embedded library is referenced with `compileOnly` (so it is on the compile classpath) plus `jarJar(...)`
  (so it is bundled). Using `implementation` would additionally drop it onto the dev runtime classpath as a
  loose dependency, which defeats the point of testing the embedded copy.
- **Fabric + production reobf:** the Fabric multiloader module also runs the Renamer to ship in the
  intermediary namespace. The Renamer remaps the base `jar`; the `jarJar` task nests into it. If you ship a
  reobfuscated Fabric jar, apply the embedding to the reobf output (or reobf before embedding) — these
  examples keep the two steps separate for clarity and verify the embedding on the dev (Mojmap) jar.
- Only the Fabric path has been verified end-to-end on a real server so far (the nested lib loads and runs).
  The Forge/NeoForge artifacts are structurally verified (metadata.json + `FMLModType` marker) against the
  format real loaders use.
