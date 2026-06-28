# MDK Examples (multi-loader ForgeGradle 7)

A collection of example Mod Developer Kits showing how to build Minecraft mods for **Forge, NeoForge and
Fabric** — including a **shared-code multiloader** setup — all driven by a single toolchain: a fork of
**ForgeGradle 7** plus a multi-loader-capable **Minecraft Mavenizer**, published to
[`maven.zznty.ru`](https://maven.zznty.ru).
  
> [!IMPORTANT]
> These are minimal examples that demonstrate specific setups. The Minecraft/loader versions shown are not
> the only ones that work, and features can be combined.

## The toolchain

| Component | Coordinate | Source |
|-----------|------------|--------|
| ForgeGradle 7 (fork) | plugin id `net.zznty.forgegradle` / `net.zznty:forgegradle` | [zznty/ForgeGradle](https://github.com/zznty/ForgeGradle) (branch `FG_7.0`) |
| Minecraft Mavenizer (fork) | `net.zznty:minecraft-mavenizer` | [zznty/MinecraftMavenizer](https://github.com/zznty/MinecraftMavenizer) |
| JarJar (fork, cross-loader) | plugin id `net.zznty.jarjar` / `net.zznty:jarjar-gradle` | [zznty/JarJar](https://github.com/zznty/JarJar) |

Both are published to `https://maven.zznty.ru/releases`. The ForgeGradle fork **defaults the Mavenizer tool
to our fork**, so consumers don't need any `fgtools` override — applying `net.zznty.forgegradle` is enough.

What the forks add over upstream Forge:
- **NeoForge** support in the Mavenizer (bridges to NeoFormRuntime), for `net.neoforged:neoforge` deps.
- **Fabric** support in the Mavenizer (merge + namespace handling), for `net.fabricmc:fabric` deps.
- **`fg.fabricMaven` / `fg.neoForgeMaven`** repository shortcuts (alongside the existing `fg.forgeMaven`).

## Quick start

In `settings.gradle`, add our maven to `pluginManagement`:

```groovy
pluginManagement {
    repositories {
        maven { url = 'https://maven.zznty.ru/releases' }
        gradlePluginPortal()
        mavenCentral()
    }
}
```

> [!NOTE]
> If you also use the **Renamer** plugin (`net.minecraftforge.renamer`) to build production jars for
> obfuscated Minecraft — as the obfuscated multiloader examples do — add the Forge maven to
> `pluginManagement` too, since that's where the Renamer plugin is hosted:
> ```groovy
> maven { url = 'https://maven.minecraftforge.net/' }
> // The Renamer has no Gradle plugin marker, so map its id to the artifact:
> resolutionStrategy.eachPlugin {
>     if (requested.id.id == 'net.minecraftforge.renamer')
>         useModule("net.minecraftforge:renamer-gradle:${requested.version}")
> }
> ```

Then apply the plugin and declare a Minecraft dependency for your loader. A minimal Fabric example:

```groovy
plugins {
    id 'java'
    id 'net.zznty.forgegradle' version '[7.0.29,8.0)'
}

minecraft {
    mappings channel: 'official', version: '26.1.2'
    runs { register('server') { args '--nogui' } }
}

repositories {
    minecraft.mavenizer(it)
    maven fg.fabricMaven          // or fg.neoForgeMaven / fg.forgeMaven
    maven fg.minecraftLibsMaven
    mavenCentral()
}

dependencies {
    implementation minecraft.dependency("net.fabricmc:fabric:26.1.2-0.19.3")
}
```

The loader coordinates are:
- Forge — `net.minecraftforge:forge:<mc>-<forge>`
- NeoForge — `net.neoforged:neoforge:<neoforge>`
- Fabric — `net.fabricmc:fabric:<mc>-<loader>` *(synthetic coordinate; mirrors the Forge one)*

## Index

### Single-loader

| Example | Loader | MC | Notes |
|---------|--------|----|-------|
| [`forge-1.21.1`](forge-1.21.1) | Forge | 1.21.1 | Stock Forge path (control). |
| [`neoforge-26.1.2`](neoforge-26.1.2) | NeoForge | 26.1.2 | Unobfuscated; NFRT bridge. |
| [`neoforge-1.21.1`](neoforge-1.21.1) | NeoForge | 1.21.1 | Obfuscated; combined-jar FML layout. |
| [`fabric-26.1.2`](fabric-26.1.2) | Fabric | 26.1.2 | Unobfuscated; official namespace, no remap. |
| [`fabric-1.21.1`](fabric-1.21.1) | Fabric | 1.21.1 | Obfuscated; intermediary/Mojmap handling. |
| [`fabric-1.20.1`](fabric-1.20.1) | Fabric | 1.20.1 | Obfuscated; older API. |

### Multiloader (shared `common` module + thin loader modules)

| Example | Loaders | MC | Notes |
|---------|---------|----|-------|
| [`multiloader-26.1.2`](multiloader-26.1.2) | Forge + NeoForge + Fabric | 26.1.2 | Unobfuscated — the simplest case; everything runs official names. |
| [`multiloader-1.21.1`](multiloader-1.21.1) | Forge + Fabric | 1.21.1 | Obfuscated; dev runs Mojmap on all loaders, production reobf via the Renamer plugin. |
| [`multiloader-1.20.1`](multiloader-1.20.1) | Forge + Fabric | 1.20.1 | Obfuscated, SRG-era Forge; production reobf for both loaders. |

See each multiloader example's own README for how the shared-code pattern and production reobfuscation work.

### Jar-in-Jar (embedding libraries, cross-loader)

Embed a plain Java library **inside** the mod jar so it loads at runtime without separate installation — across
all loaders, via the `net.zznty.jarjar` plugin (a cross-loader fork of Forge's JarJar). The `loader` property
emits the right metadata per loader (`metadata.json` + `FMLModType` for Forge/NeoForge; patched
`fabric.mod.json` for Fabric).

| Example | Loader(s) | MC | Notes |
|---------|-----------|----|-------|
| [`jarjar/forge-1.21.1`](jarjar/forge-1.21.1) | Forge | 1.21.1 | `loader = 'forge'`. |
| [`jarjar/neoforge-26.1.2`](jarjar/neoforge-26.1.2) | NeoForge | 26.1.2 | `loader = 'neoforge'` (same format as Forge). |
| [`jarjar/fabric-26.1.2`](jarjar/fabric-26.1.2) | Fabric | 26.1.2 | `loader = 'fabric'` (the special case). |
| [`jarjar/multiloader-1.21.1`](jarjar/multiloader-1.21.1) | Forge + Fabric | 1.21.1 | One shared lib embedded into both jars; `loader = project.name`. |
| [`jarjar/multiloader-1.20.1`](jarjar/multiloader-1.20.1) | Forge + Fabric | 1.20.1 | Obfuscated: Jar-in-Jar + production reobf (SRG / intermediary) on the same jar. |

See [`jarjar/README.md`](jarjar/README.md) for the full explanation.

### Mixins (obfuscated vs direct)

SpongePowered Mixin examples showing how refmaps and reobf differ between obfuscated and direct (unobfuscated)
MC. Both inject into `DedicatedServer.initServer()`.

| Example | Loader | MC | Runtime namespace | Refmap? |
|---------|--------|----|----|---------|
| [`mixins/forge-1.20.1`](mixins/forge-1.20.1) | Forge | 1.20.1 | SRG | Yes (Mojmap → SRG via renamer `enableMixinRefmaps`) |
| [`mixins/forge-1.21.1`](mixins/forge-1.21.1) | Forge | 1.21.1 | Mojmap | No (`remap = false`, dev = runtime) |

See [`mixins/README.md`](mixins/README.md) for the full explanation.

## How it works (high level)

- **Single mapping namespace.** Every module compiles against **official (Mojang) mappings**. On
  unobfuscated Minecraft (26.1+) all loaders also *run* in official names, so shared `net.minecraft.*` code
  links and runs everywhere with no translation. On obfuscated Minecraft the Mavenizer delivers each loader's
  Minecraft in the Mojmap (`named`) dev namespace so the same thing holds in development.
- **Shared code, compiled in.** The multiloader `common` module exposes its sources; each loader module
  compiles them straight into its jar (no jar-in-jar, no per-loader remap in dev).
- **Production builds.** For obfuscated versions a shippable jar must be reobfuscated to the loader's runtime
  namespace (SRG for older Forge, intermediary for Fabric). This is done with the
  `net.minecraftforge.renamer` plugin — see the multiloader examples.

## Requirements

- JDK to run Gradle. Minecraft 26.x examples use a Java 25 toolchain, 1.21.1 uses 21, 1.20.1 uses 17 —
  Gradle auto-provisions the toolchain, you just need a JDK to launch Gradle itself.
- No manual setup of the toolchain: ForgeGradle and the Mavenizer are resolved from `maven.zznty.ru`.
