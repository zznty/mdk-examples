# Mixin examples (obf vs direct, all loaders)

[SpongePowered Mixin](https://github.com/SpongePowered/Mixin) examples with ForgeGradle 7, covering both
**obfuscated** MC (needs a refmap + reobf) and **direct/unobfuscated** MC (no refmap), across Forge, NeoForge
and Fabric. Every example injects into `DedicatedServer.initServer()` and logs when the server starts.

| Example | Loader | MC | Runtime namespace | Refmap | Mixin config declared in |
|---------|--------|----|----|--------|--------------------------|
| [`forge-1.21.1`](forge-1.21.1) | Forge | 1.21.1 | Mojmap | none (`remap = false`) | jar manifest `MixinConfigs` |
| [`forge-1.20.1`](forge-1.20.1) | Forge | 1.20.1 | SRG | Mojmap → SRG | jar manifest `MixinConfigs` |
| [`neoforge-1.21.1`](neoforge-1.21.1) | NeoForge | 1.21.1 | Mojmap | none (`remap = false`) | jar manifest (or `neoforge.mods.toml`) |
| [`fabric-26.1.2`](fabric-26.1.2) | Fabric | 26.1.2 | Mojmap | none (`remap = false`) | `fabric.mod.json` `"mixins"` |
| [`fabric-1.20.1`](fabric-1.20.1) | Fabric | 1.20.1 | intermediary | Mojmap → intermediary | `fabric.mod.json` `"mixins"` |

## The core idea: dev namespace vs runtime namespace

You always **compile** against Mojmap (official) names. Whether you need a **refmap** depends on what the
loader **runs**:

- **Runtime == Mojmap** (Forge/NeoForge on modern MC, Fabric on unobf 26.x): the names match, so no refmap is
  needed. Annotate mixin members with `remap = false` so the AP doesn't try to look up an obfuscation mapping
  (which would fail — there's nothing to map to).
- **Runtime != Mojmap** (Forge 1.20.1 → SRG, Fabric 1.20.1 → intermediary): the mixin annotation processor
  generates a **refmap** translating Mojmap → the runtime namespace, and the production jar is reobfed. The
  [Renamer](https://github.com/MinecraftForge/Renamer) plugin's `enableMixinRefmaps` configures the AP and
  remaps the refmap during reobf.

## Direct / unobfuscated (no refmap)

```java
// remap = false: dev namespace IS the runtime namespace, so look up the method by its Mojmap name directly.
@Inject(method = "initServer", at = @At("RETURN"), remap = false)
```

```groovy
dependencies {
    annotationProcessor 'org.spongepowered:mixin:0.8.7:processor'
}
```

The shipped jar is the plain `jar` output — no `-srg`/`-intermediary` reobf.

## Obfuscated (refmap + reobf)

```java
// remap = true (default): the AP generates a refmap entry for this target.
@Inject(method = "initServer", at = @At("RETURN"))
```

```groovy
renamer.enableMixinRefmaps { config "${mod_id}.mixins.json" }
renamerTools { configure("classes") { artifact = "net.minecraftforge:renamer:2.2.3:all" } }

// Forge (SRG): the Mavenizer exposes the mapping as a coordinate.
renamer.mappings(minecraft.dependency.toSrg)
renamer.classes(tasks.named('jar', Jar)) {
    map.from minecraft.dependency.toSrgFile
    archiveClassifier = 'srg'
    mappings renamer.mixin.generatedMappings
}
```

The generated `main.refmap.json` looks like (Forge 1.20.1):

```json
{ "data": { "searge": { "DedicatedServerMixin": { "initServer": "Lnet/minecraft/server/dedicated/DedicatedServer;m_7038_()Z" } } } }
```

and for Fabric 1.20.1 (intermediary — `class_3176` = `DedicatedServer`, `e()Z` = the intermediary method):

```json
{ "data": { "searge": { "DedicatedServerMixin": { "initServer": "Lnet/minecraft/class_3176;e()Z" } } } }
```

(The namespace key is always `searge` by mixin convention; the *values* are whatever the runtime uses.)

## Loader-specific notes

### Forge / NeoForge
- Both **bundle Mixin** at runtime, so only the `annotationProcessor` dependency is needed (it also carries the
  annotations for compilation).
- The mixin config is registered via the jar manifest `MixinConfigs` attribute. NeoForge additionally supports
  a `[[mixins]]` block in `neoforge.mods.toml`.
- NeoForge 1.21.1 (and modern Forge) run **Mojmap** in production → no refmap. Forge 1.20.1 runs **SRG** → refmap.

### Fabric
- Fabric does **not** expose Mixin annotations on the compile classpath, so add an explicit
  `compileOnly 'org.spongepowered:mixin:0.8.7'` alongside the processor. The Mixin artifact isn't on Maven
  Central — it's hosted on the Forge maven (`fg.forgeMaven`).
- Mixin configs are declared in `fabric.mod.json` under `"mixins"`, not the jar manifest.
- For obfuscated MC the Mavenizer exposes the Mojmap→intermediary mapping both as a resolvable coordinate
  (`minecraft.dependency.toObf`) and as a file (`minecraft.dependency.toObfFile`) — just like Forge's
  `toSrg` / `toSrgFile`. The mixin-refmap path needs the coordinate, so use
  `renamer.mappings(minecraft.dependency.toObf)` (requires Mavenizer ≥ 0.5.28 / ForgeGradle ≥ 7.0.38).
