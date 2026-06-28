# Jar-in-Jar multiloader (Forge + Fabric, MC 1.21.1)

A shared-code **multiloader** mod that embeds the same plain library (`greetlib`) into **both** loader jars via
cross-loader Jar-in-Jar. One `common` module holds the shared code (which calls `greetlib`); thin `forge` and
`fabric` modules wrap it. All on **ForgeGradle 7**, compiled against **official (Mojang) mappings**.

```
multiloader-1.21.1/
├── settings.gradle     # includes greetlib, common, fabric, forge
├── build.gradle        # shared config; loader block applies net.zznty.jarjar per module
├── greetlib/           # plain Java library (no Minecraft, no loader) — the embedded artifact
├── common/             # clean (loader-free) Minecraft; shared code, calls greetlib
├── fabric/             # net.fabricmc:fabric  + fabric.mod.json
└── forge/              # net.minecraftforge:forge + META-INF/mods.toml
```

## The one interesting line

The shared `greetlib` is embedded into each loader jar with the **same** dependency wiring; only the loader
*flavour* differs, and it is derived from the module name:

```groovy
configure(loaderModules) {            // forge + fabric
    apply plugin: 'net.zznty.jarjar'
    jarJar.register {
        loader = project.name         // 'forge' or 'fabric'
    }
    dependencies {
        compileOnly project(':greetlib')
        jarJar(project(':greetlib')) {
            jarJar.configure(it) { version = '1.0.0' }
        }
    }
}
```

From that single `loader = project.name`, the two shipped `-all.jar`s come out structured for their loader:

| Module | `loader` | Nested-jar reference | greetlib marker |
|--------|----------|----------------------|------------------|
| `forge`  | `forge`  | `META-INF/jarjar/metadata.json` | `FMLModType: LIBRARY` in MANIFEST |
| `fabric` | `fabric` | patched `fabric.mod.json` `"jars"` | synthesized `fabric.mod.json` inside greetlib |

`greetlib` is a plain library, so it is deliberately excluded from the `net.zznty.forgegradle` `subprojects`
block (it is neither a Minecraft module nor a loader module) and configured solely by its own `build.gradle`.

## Build

```bash
./gradlew build
# forge/build/libs/multiloader_test-forge-1.0-all.jar    -> ship this on Forge
# fabric/build/libs/multiloader_test-fabric-1.0-all.jar  -> ship this on Fabric (dev/Mojmap namespace)
```

See [`../README.md`](../README.md) for how each loader loads nested jars, and the note on combining Fabric
production reobf (intermediary namespace) with Jar-in-Jar.
