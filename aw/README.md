# Access Widener example (multiloader, obfuscated MC 1.20.1)

A multiloader (Forge + Fabric) example showing how to use Access Wideners — a single
`.accesswidener` file that widens access to protected/private Minecraft members, working across
**both** loaders on obfuscated MC 1.20.1 (the hardest case: Forge → SRG runtime, Fabric → intermediary runtime).

```
aw/multiloader-1.20.1/
├── build.gradle              # accessWidener = "${mod_id}.accesswidener" on all loader modules
├── common/                   # shared code calls DedicatedServer.initServer() (widened)
├── forge/                    # AW→AT by Mavenizer, runtime test via ServerAboutToStartEvent
│   └── src/main/resources/multiloader_test.accesswidener
├── fabric/                   # AW consumed natively by Fabric Loader, fabric.mod.json reference
│   └── src/main/resources/fabric.mod.json  ("accessWidener": "multiloader_test.accesswidener")
│   └── src/main/resources/multiloader_test.accesswidener
└── gradle.properties
```

## How it works

The **same** `.accesswidener` file (`v2 named` format, Mojang namespace) is placed in each loader
module's resources:

```
accessWidener	v2	named
accessible	class	net/minecraft/server/dedicated/DedicatedServer
accessible	method	net/minecraft/server/dedicated/DedicatedServer	initServer	()Z
```

### Forge path

1. ForgeGradle resolves the AW from the source set and passes `--access-widener` to the Mavenizer (0.5.31+)
2. The Mavenizer converts the AW to an Access Transformer (`.cfg`) with SRG names using the cached
   Mojmap→SRG mapping:
   ```
   public ahe        (DedicatedServer → ahe in SRG)
   public ahe e()Z   (initServer → e()Z in SRG)
   ```
3. The converted AT is merged with any existing ATs and applied to the Minecraft jar during deobfuscation
4. The mod compiles against the widened jar — `initServer()` is now accessible

This is a **two-pass** process: the first Gradle build generates the SRG mapping (cached by MCPConfig),
and the second build uses it to convert AW→AT. In practice this means a fresh clone needs two builds.

### Fabric path

1. The AW file is included in the jar's resources (from the source set)
2. `fabric.mod.json` references it: `"accessWidener": "multiloader_test.accesswidener"`
3. At dev time, the Mavenizer does NOT convert it (Fabric handles AWs natively at runtime)
4. At runtime, the Fabric Loader reads the AW from the jar and applies the widening

## DSL

The `accessWidener` property is on the `minecraft` extension (parallel to `accessTransformer`):

```groovy
minecraft {
    mappings channel: 'official', version: mc_version
    accessWidener = "${mod_id}.accesswidener"   // resolves from src/main/resources/
}
```

It follows the same convention cascade as `accessTransformer`: the loader modules can override
it per-dependency, and the default path is `META-INF/${mod_id}.accesswidener`.

## Build

```bash
./gradlew build
# forge/build/libs/multiloader_test-forge-1.0-srg.jar    # Forge ship (SRG prod)
# fabric/build/libs/multiloader_test-fabric-1.0-intermediary.jar  # Fabric ship (intermediary prod)
```

## Differences from architectury-loom's AW→AT

| | architectury-loom | ForgeGradle 7 (this fork) |
|---|---|---|
| **AW→AT conversion** | Gradle task (`Aw2AtAction`) runs during jar build | Mavenizer CLI converts during MC deobfuscation |
| **AT ships in the jar** | Yes (AT `.cfg` bundled in mod jar) | No (AT baked into MC dev jar, mod doesn't need it at runtime) |
| **Namespace origin** | AW is in `named` (Mojmap), AT in SRG | Same |
| **Factory handling** | AW is optional per-loader via `architectury.common.json` | AW declared in `minecraft` extension DSL, applies to all Forge/NeoForge deps |
