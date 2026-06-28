# Mixin examples (obf vs direct)

Two Forge examples showing how to use [SpongePowered Mixin](https://github.com/SpongePowered/Mixin) with
ForgeGradle 7 — one on **obfuscated** MC (needs a refmap + reobf) and one on **direct/unobfuscated** MC
(no refmap needed). The key difference is whether the dev namespace (Mojmap) matches the runtime namespace.

| Example | MC | Runtime namespace | Refmap? | Reobf? |
|---------|----|----|---------|--------|
| [`forge-1.20.1`](forge-1.20.1) | 1.20.1 | SRG | Yes (Mojmap → SRG via renamer) | Yes (`-srg.jar`) |
| [`forge-1.21.1`](forge-1.21.1) | 1.21.1 | Mojmap | No (`remap = false`) | No |

Both examples inject into `DedicatedServer.initServer()` and log when the server starts.

## How it works

### Obfuscated MC (1.20.1, SRG production)

Forge 1.20.1 runs **SRG** names in production, but you compile against **Mojmap** (official) names. The mixin
annotation processor (AP) generates a **refmap** that translates between them:

```json
// main.refmap.json (inside the jar)
{
  "data": {
    "searge": {
      "DedicatedServerMixin": {
        "initServer": "Lnet/minecraft/server/dedicated/DedicatedServer;m_7038_()Z"
      }
    }
  }
}
```

At runtime the mixin transformer reads the refmap, resolves `initServer` → `m_7038_`, and injects into the
correct SRG method. The [renamer](https://github.com/MinecraftForge/Renamer) plugin's `enableMixinRefmaps`
configures the AP (so it knows the SRG mapping) and `mixin.generatedMappings` feeds the AP's output into the
reobf step. The shipped jar is **`-srg.jar`** (reobfed mod classes + remapped refmap).

```groovy
renamer.enableMixinRefmaps { config "${mod_id}.mixins.json" }
renamer.mappings(minecraft.dependency.toSrg)
renamer.classes(tasks.named('jar', Jar)) {
    map.from minecraft.dependency.toSrgFile
    archiveClassifier = 'srg'
    mappings renamer.mixin.generatedMappings
}
```

### Direct / unobfuscated MC (1.21.1, Mojmap production)

Forge 1.21.1 runs **Mojmap** in production — the same namespace you compile against. No translation is needed,
so there's no refmap and no reobf. The only requirement is `remap = false` on mixin annotations that reference
method names, so the AP doesn't try (and fail) to look up an obfuscation mapping:

```java
@Inject(method = "initServer", at = @At("RETURN"), remap = false)
```

The shipped jar is the plain `jar` output (no `-srg.jar`).

## Common setup (both examples)

```groovy
dependencies {
    implementation minecraft.dependency("net.minecraftforge:forge:${minecraft_version}-${forge_version}")
    annotationProcessor 'org.spongepowered:mixin:0.8.7:processor'
}

tasks.named('jar', Jar) {
    manifest {
        attributes['MixinConfigs'] = "${mod_id}.mixins.json"
    }
}
```

Forge bundles Mixin at runtime, so only the `annotationProcessor` dependency is needed (for the AP and the
annotations). The `MixinConfigs` manifest attribute tells the loader to pick up the mixin config automatically.
