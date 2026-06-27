# Multiloader Test (obfuscated) — shared-code Forge + Fabric on ForgeGradle 7, MC 1.21.1

A shared-code **multiloader** mod for **obfuscated** Minecraft (1.21.1). Like
[`../multiloader-test/`](../multiloader-test/) (which targets unobfuscated 26.1.2), one `common` module holds
the cross-loader code and thin `forge` / `fabric` modules wrap it — all on **ForgeGradle 7**, all compiled
against **official (Mojang) mappings**.

> NeoForge is intentionally omitted here (you asked for Forge + Fabric). It would work the same way as Forge.

```
multiloader-1.21.1/
├── settings.gradle            # includes common, fabric, forge
├── gradle.properties          # MC 1.21.1, forge 52.1.14, fabric-loader 0.19.3, java 21
├── build.gradle               # ALL shared config: subprojects{} + loader-only block
├── common/                    # clean (loader-free) Minecraft; the shared code lives here
├── fabric/                    # net.fabricmc:fabric:<mc>-<loader>  + fabric.mod.json
└── forge/                     # net.minecraftforge:forge:<mc>-<forge> + META-INF/mods.toml
```

## The obfuscated-multiloader problem (and how it's solved)

On **26.1.2** the shared-code approach is free: Minecraft is unobfuscated, so every loader runs official
(Mojang) names and the same `net.minecraft.*` sources compile and run everywhere.

On **obfuscated** Minecraft (≤ 1.21.11) the game classes are scrambled, and each loader normally uses a
*different* namespace at dev runtime:

| Loader | classic dev namespace |
|--------|------------------------|
| Forge / NeoForge | **Mojmap** (official names) |
| Fabric | **intermediary** (`class_2960` …) |

If Fabric delivered an intermediary-named Minecraft, the Fabric subproject couldn't even compile the shared
Mojmap code (`package net.minecraft.resources does not exist`). So **every loader here is configured to use
Mojmap in dev** — the same thing the canonical MultiLoader-Template does (`officialMojangMappings()` on the
Fabric side).

Our Mavenizer fork makes this work: when a Fabric dependency is requested with `mappings channel: 'official'`
on obfuscated Minecraft, it remaps the merged jar **notch → `named`** (Fabric's term for the Mojmap dev
namespace) using Minecraft's own client/server mappings — instead of notch → intermediary. The game then runs
in the `named` namespace with no further runtime remap, exactly like Forge/NeoForge run Mojmap. The common
module's single declaration (`mappings channel: 'official'`, shared by all modules in the root build.gradle)
is all that's needed.

## Production / non-dev

Dev runs Mojmap on every loader; a shippable jar for a normal (non-dev) installation needs the per-loader
"obfuscation" step, handled here by the **`net.minecraftforge.renamer`** plugin:

* **Forge 1.21.1 (52.x)** runs Mojmap in production too, so the normal `jar` ships as-is — no reobf.
* **Fabric** ships in the **intermediary** namespace, so `:fabric:renameJar` reobfs the Mojmap jar →
  intermediary using the `Mojmap → intermediary` mapping the Mavenizer publishes
  (`minecraft.dependency.toObfFile`). Output: `…-fabric-1.0-intermediary.jar`.

```bash
./gradlew :forge:jar              # Forge prod jar (Mojmap, ships as-is)
./gradlew :fabric:renameJar       # Fabric prod jar  -> build/libs/…-intermediary.jar
```

**Verified in real, non-dev environments:** the Forge jar loads on an official Forge 1.21.1 dedicated
server, and the intermediary jar loads on an official Fabric 1.21.1 server (downloaded via the Fabric server
launcher) — both running the same shared `common` code (including methods that the production mapping
renames, so a broken reobf would crash).

> The renamer can also remap mixin refmaps (`renamer.enableMixinRefmaps`); this example has no mixins, so it
> only remaps classes.

## Running (dev)

```bash
./gradlew :forge:build  :fabric:build       # compile both loader jars (shared common code merged in)
./gradlew :forge:runServer                  # boots a dedicated server…
./gradlew :fabric:runServer
./gradlew :forge:runClient                  # …or client
./gradlew :fabric:runClient
```

Each run prints the same shared line from the `common` module:

```
[multiloader_test] Hello from shared common code, running on Forge!  (sample MC id: minecraft:diamond)
[multiloader_test] Hello from shared common code, running on Fabric! (sample MC id: minecraft:diamond)
```

## Prerequisites

None beyond a JDK to launch Gradle. The toolchain (ForgeGradle `net.zznty.forgegradle` + the Minecraft Mavenizer)
is resolved automatically from `https://maven.zznty.ru/releases` (declared in `settings.gradle`).


## Verified

Both Forge and Fabric **compile the shared Mojmap `common` code into their jars** and **boot a dedicated
server** on MC 1.21.1, each executing the same `ExampleMod.init(...)`.
