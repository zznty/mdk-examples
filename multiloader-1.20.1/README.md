# Multiloader Test (obfuscated) — shared-code Forge + Fabric on ForgeGradle 7, MC 1.20.1

Same shape as [`../multiloader-1.21.1/`](../multiloader-1.21.1/) — a shared-code `common` module plus thin
`forge` and `fabric` modules, all on ForgeGradle 7, all compiled against **official (Mojang) mappings** — but
targeting **MC 1.20.1** (forge 47.4.20, fabric-loader 0.19.3, Java 17).

See the [1.21.1 README](../multiloader-1.21.1/README.md) for the full explanation of how the obfuscated
multiloader works (Fabric is delivered in the Mojmap `named` namespace so the shared Mojmap code compiles and
runs on every loader in dev).

## 1.20.1-specific note: production reobf

1.20.1 sits in the **SRG-era** of Forge (47.x), which differs from 1.21.1 for *production* shipping:

| | dev namespace | prod (non-dev) namespace | prod reobf (`net.minecraftforge.renamer`) |
|---|---|---|---|
| Forge 1.20.1 | Mojmap | **SRG** | `:forge:renameJar` — Mojmap → SRG (`minecraft.dependency.toSrgFile`) |
| Fabric 1.20.1 | Mojmap (`named`) | **intermediary** | `:fabric:renameJar` — Mojmap → intermediary (`…toObfFile`) |

In **dev** both run Mojmap, so the shared code works directly. For shipping, **both** loaders here need
reobf (unlike 1.21.1, where Forge ships Mojmap as-is) — handled by the same `net.minecraftforge.renamer`
plugin, just with different mapping files:

```bash
./gradlew :forge:renameJar        # -> build/libs/…-forge-1.0-srg.jar          (SRG)
./gradlew :fabric:renameJar       # -> build/libs/…-fabric-1.0-intermediary.jar (intermediary)
```

**Verified in real, non-dev environments:** the `-srg` jar loads on an official Forge 1.20.1 server and the
`-intermediary` jar on an official Fabric 1.20.1 server. The shared `common` code calls methods that the
production mapping renames (`getPath()` → `m_135815_`/`method_12832`, `getDescriptionId()` →
`m_5524_`/`method_7876`), so both confirm the reobf actually resolves at runtime rather than silently
"working".

The shared `common` code uses `new ResourceLocation("minecraft", "diamond")` (the 1.20.1 API) rather than
1.21.1's `ResourceLocation.parse(...)` — a reminder that the *shared* code must use API present in the
targeted Minecraft version.

## Running (dev)

```bash
./gradlew :forge:build  :fabric:build
./gradlew :forge:runServer    # or :fabric:runServer / :forge:runClient / :fabric:runClient
```

Each prints the same shared line:

```
[multiloader_test] Hello from shared common code, running on Forge!  (sample MC id: minecraft:diamond)
[multiloader_test] Hello from shared common code, running on Fabric! (sample MC id: minecraft:diamond)
```

## Verified

Both Forge and Fabric **compile the shared Mojmap `common` code into their jars** and **boot a dedicated
server** on MC 1.20.1.
