# Jar-in-Jar multiloader (Forge + Fabric, MC 1.20.1, obfuscated)

Same shared-code multiloader pattern as [`../multiloader-1.21.1/`](../multiloader-1.21.1/), but on
**obfuscated MC 1.20.1** — where both loaders need production reobf (Forge → SRG, Fabric → intermediary).
The interesting part is how Jar-in-Jar and the reobf **coexist**: the shipped jar must have both reobfed mod
classes *and* the nested library.

```
multiloader-1.20.1/
├── settings.gradle     # includes greetlib, common, fabric, forge
├── build.gradle        # shared config; loader block applies net.zznty.jarjar per module
├── greetlib/           # plain Java library (no Minecraft, no loader) — the embedded artifact
├── common/             # clean (loader-free) Minecraft; shared code, calls greetlib
├── fabric/             # net.fabricmc:fabric  + fabric.mod.json
└── forge/              # net.minecraftforge:forge + META-INF/mods.toml
```

## How reobf + Jar-in-Jar work together

`greetlib` is a plain Java library with **no Minecraft references** — it doesn't need reobf. Only the mod's
classes (which call `net.minecraft.*`) must be remapped. So the production flow is:

1. `jar` — base dev jar (Mojmap classes + metadata.json, no nesting)
2. `jarJar` — dev jar + nested greetlib (Mojmap classes + nesting) → `-all.jar`
3. `renameJarJar` — reobf the `-all.jar`: remaps the mod's `.class` files, leaves the nested jar (a binary
   entry) untouched → `-all-srg.jar` / `-all-intermediary.jar`

The renamer is registered on the **`jarJar` task** (not the base `jar`), so the reobfed output carries the
nested library:

```groovy
// forge/build.gradle
renamer.classes(tasks.named('jarJar', Jar)) {
    map.from minecraft.dependency.toSrgFile
    archiveClassifier = 'srg'
}

// fabric/build.gradle
renamer.classes(tasks.named('jarJar', Jar)) {
    map.from minecraft.dependency.toObfFile
    archiveClassifier = 'intermediary'
}
```

Verified bytecode: the `-all-srg.jar` mod class references SRG method names (`m_XXXXX_`, no Mojmap), and the
`-all-intermediary.jar` references intermediary names (`method_XXXXX`, no Mojmap) — while both still carry the
nested greetlib with its loader-specific marker.

## Build outputs

```
./gradlew build

# Forge
forge/build/libs/multiloader_test-forge-1.0-all.jar        # dev (Mojmap) + nested greetlib
forge/build/libs/multiloader_test-forge-1.0-all-srg.jar    # SHIP THIS (SRG + nested greetlib)

# Fabric
fabric/build/libs/multiloader_test-fabric-1.0-all.jar              # dev (Mojmap) + nested greetlib
fabric/build/libs/multiloader_test-fabric-1.0-all-intermediary.jar # SHIP THIS (intermediary + nested greetlib)
```
