# Multiloader Test — shared-code Forge + NeoForge + Fabric on ForgeGradle 7

A traditional **multiloader** mod: one `common` module holds the cross-loader code, and three thin loader
modules (`forge`, `neoforge`, `fabric`) wrap it. Everything is built with a **single toolchain — ForgeGradle
7** — using our multi-loader-capable toolchain published to maven.zznty.ru.

Where the well-known [MultiLoader-Template](https://github.com/jaredlll08/MultiLoader-Template) mixes three
plugins (Loom + ModDevGradle + ForgeGradle), here **all four modules use ForgeGradle 7**.

All the shared configuration lives in the **root `build.gradle`** via `subprojects {}` blocks — there is no
`buildSrc` / convention plugins to maintain (the same lightweight approach as a typical Architectury
project). Each module's own `build.gradle` is then tiny: just its Minecraft/loader coordinate.

```
multiloader-test/
├── settings.gradle            # includes common, fabric, neoforge, forge
├── gradle.properties          # one place for MC + loader versions, mod metadata
├── build.gradle               # ALL shared config: subprojects{} (every module) + loader-only block
├── common/                    # clean (loader-free) Minecraft; the shared code lives here
│   ├── build.gradle           # just the net.minecraft:joined dep + commonJava/commonResources exports
│   └── src/main/java/.../ExampleMod.java        # touches net.minecraft.* directly
├── fabric/    build.gradle (1 dep) + src/main/java/.../fabric/FabricExampleMod.java     + fabric.mod.json
├── neoforge/  build.gradle (1 dep) + src/main/java/.../neoforge/NeoForgeExampleMod.java + META-INF/neoforge.mods.toml
└── forge/     build.gradle (1 dep) + src/main/java/.../forge/ForgeExampleMod.java       + META-INF/mods.toml
```

The root `build.gradle` has two blocks:

* `subprojects { … }` — applied to **every** module: the FG7 plugin (`net.zznty.forgegradle`),
  `mappings channel: 'official'`, the repositories, and the resource templating.
* `configure(subprojects.findAll { it.name != 'common' }) { … }` — applied to the **loader** modules only:
  the `commonJava`/`commonResources` wiring that compiles the shared sources in, plus the `client`/`server`
  run tasks.

Target: **Minecraft 26.1.2** (unobfuscated — every loader runs official/Mojang names at runtime, so no
mapping translation is needed anywhere).

## How the shared module works

The whole approach hinges on **one mapping namespace everywhere: official (Mojang)**.

1. **`common` compiles against clean, loader-free Minecraft.** It declares
   `minecraft.dependency("net.minecraft:joined:26.1.2")` — the Mavenizer produces a merged client+server
   vanilla jar in official names. No loader, no loader API; shared code may only touch `net.minecraft.*`.

2. **`common` exposes its raw sources/resources** as consumable configurations (`commonJava`,
   `commonResources`) — it does *not* publish a jar that gets bundled.

3. **Each loader module compiles `common`'s sources straight into its own jar.** The loader-only block in
   the root `build.gradle` adds:
   ```groovy
   compileOnly project(':common')                                   // IDE/compiler resolution only
   commonJava      project(path:':common', configuration:'commonJava')
   commonResources project(path:':common', configuration:'commonResources')
   compileJava     { source(configurations.commonJava) }            // <- common code compiled INTO the jar
   processResources{ from(configurations.commonResources) }
   ```
   Because every module is compiled against the same official mappings, this needs **no per-loader
   remapping and no jar-in-jar** — the shared classes end up natively in each loader jar.

Each loader's entry point is a one-liner that calls into the shared code:

```java
// fabric
public final class FabricExampleMod implements ModInitializer {
    public void onInitialize() { ExampleMod.init("Fabric"); }
}
// neoforge / forge
@Mod(ExampleMod.MOD_ID)
public final class NeoForgeExampleMod { public NeoForgeExampleMod() { ExampleMod.init("NeoForge"); } }
```

## Two FG7-specific gotchas (and the fixes)

* **`net.minecraftforge.gradle.merge-source-sets=true`** (in `gradle.properties`). FML discovers a mod by
  scanning a *single* root that holds both its `mods.toml` and its `@Mod` class. By default Gradle splits
  compiled classes (`build/classes/java/main`) from resources (`build/resources/main`), so FML reports
  *"has mods that were not found"*. This property makes ForgeGradle emit both into one unified output dir.
  (Loom and ModDevGradle do the equivalent internally; on FG7 it's this opt-in.) Fabric/NeoForge don't
  strictly need it, but it's harmless and keeps all three consistent.

* **Version-range syntax differs per loader.** FML's tomls use Maven ranges (`[26.1,27)`); Fabric's
  `fabric.mod.json` uses semver predicates and rejects Maven brackets, so it uses `"minecraft": "*"`.

## Running

```bash
# from this directory
./gradlew :fabric:build :neoforge:build :forge:build   # compile all three loader jars

./gradlew :fabric:runServer        # each boots a dedicated server…
./gradlew :neoforge:runServer
./gradlew :forge:runServer

./gradlew :fabric:runClient        # …or client
./gradlew :neoforge:runClient
./gradlew :forge:runClient
```

Each run prints the shared line from the `common` module, proving the same code executes on every loader:

```
[multiloader_test] Hello from shared common code, running on Fabric!   (sample MC id: minecraft:diamond)
[multiloader_test] Hello from shared common code, running on NeoForge! (sample MC id: minecraft:diamond)
[multiloader_test] Hello from shared common code, running on Forge!    (sample MC id: minecraft:diamond)
```

## Prerequisites

None beyond a JDK to launch Gradle. The toolchain (ForgeGradle `net.zznty.forgegradle` + the Minecraft Mavenizer)
is resolved automatically from `https://maven.zznty.ru/releases` (declared in `settings.gradle`).


## Verified

All three loaders **compile the shared `common` code into their jars** and **boot both client and server**
on MC 26.1.2, each executing the same `ExampleMod.init(...)`.

## Not covered yet

This is the *traditional* source-merge multiloader baseline. Still to explore (later): jar-in-jar bundling
of the common module instead of source-merging, shared/published dependencies, mixins in common, data
generation, and obfuscated-MC versions (≤1.21.11, where Fabric runs in intermediary and the namespaces no
longer line up for free).
