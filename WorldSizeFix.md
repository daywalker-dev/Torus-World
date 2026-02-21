# WorldSize Mod â€” Fixing the 3 Compilation Issues (MC 1.21.11 Mojang Mappings)

## Issue 1: `Cannot resolve method 'serverLevel' in 'ServerPlayer'`

In Minecraft 1.21.11 with Mojang mappings, `ServerPlayer` does **not** have a method called `serverLevel()`. The correct way to get the `ServerLevel` from an entity (including `ServerPlayer`) is:

```java
// WRONG (doesn't exist in 1.21.11 Mojang mappings):
ServerLevel level = player.serverLevel();

// CORRECT â€” use level() inherited from Entity, then cast:
ServerLevel level = (ServerLevel) player.level();
```

**Why**: In Mojang mappings for 1.21.11, `Entity` has a `level()` method that returns a `Level`. Since `ServerPlayer` only exists on the server side, you can safely cast to `ServerLevel`. There is no dedicated `serverLevel()` accessor on `ServerPlayer` in this version.

If you're in a context where you already know you're on the server (e.g., a server tick event), the cast is safe:

```java
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

// In your tick handler or event:
ServerLevel serverLevel = (ServerLevel) player.level();
```

---

## Issue 2: `Cannot resolve symbol 'WorldGenRegion'`

`WorldGenRegion` **does exist** in 1.21.11 Mojang mappings. The issue is likely a missing import. The full qualified name is:

```java
import net.minecraft.server.level.WorldGenRegion;
```

It is in the `net.minecraft.server.level` package, same as `ServerLevel` and `ServerPlayer`.

If you're getting this error, double-check:
1. Your import statement uses the correct package: `net.minecraft.server.level.WorldGenRegion`
2. Your Gradle project is properly set up with `mappings loom.officialMojangMappings()` (which you already have in your build.gradle)
3. You've refreshed your Gradle project after making changes to build.gradle (in IntelliJ: click the Gradle elephant icon â†’ "Reload All Gradle Projects")

**If you're using this in a Mixin**, make sure the mixin target class is correct. `WorldGenRegion` implements `WorldGenLevel`, so if your mixin targets `WorldGenLevel`, you'd reference it differently.

---

## Issue 3: Where to put `normal.json` (the world preset file)

The world preset JSON file goes in your mod's **resources** directory under the data pack structure. Specifically:

```
src/main/resources/
â”œâ”€â”€ fabric.mod.json
â”œâ”€â”€ worldsize.mixins.json
â”œâ”€â”€ assets/
â”‚   â””â”€â”€ worldsize/
â”‚       â””â”€â”€ lang/
â”‚           â””â”€â”€ en_us.json          â† display name for the preset
â””â”€â”€ data/
    â”œâ”€â”€ worldsize/
    â”‚   â””â”€â”€ worldgen/
    â”‚       â””â”€â”€ world_preset/
    â”‚           â””â”€â”€ torus.json      â† your world preset definition
    â””â”€â”€ minecraft/
        â””â”€â”€ tags/
            â””â”€â”€ worldgen/
                â””â”€â”€ world_preset/
                    â””â”€â”€ normal.json â† adds your preset to the menu
```

### File contents:

**`data/worldsize/worldgen/world_preset/torus.json`** â€” Your world preset:
```json
{
  "dimensions": {
    "minecraft:overworld": {
      "type": "minecraft:overworld",
      "generator": {
        "type": "worldsize:torus"
      }
    },
    "minecraft:the_nether": {
      "type": "minecraft:the_nether",
      "generator": {
        "type": "minecraft:noise",
        "settings": "minecraft:nether",
        "biome_source": {
          "type": "minecraft:multi_noise",
          "preset": "minecraft:nether"
        }
      }
    },
    "minecraft:the_end": {
      "type": "minecraft:the_end",
      "generator": {
        "type": "minecraft:noise",
        "settings": "minecraft:end",
        "biome_source": {
          "type": "minecraft:the_end"
        }
      }
    }
  }
}
```

**`data/minecraft/tags/worldgen/world_preset/normal.json`** â€” Makes it appear in the "World Type" button:
```json
{
  "replace": false,
  "values": [
    "worldsize:torus"
  ]
}
```

**`assets/worldsize/lang/en_us.json`** â€” Display name:
```json
{
  "generator.worldsize.torus": "Torus World"
}
```

### Key points:
- The world preset goes under `data/<your_mod_id>/worldgen/world_preset/`
- To make it show in the world creation screen, you must add it to the `minecraft:normal` tag at `data/minecraft/tags/worldgen/world_preset/normal.json`
- The `"type": "worldsize:torus"` in the generator must match the `Identifier` you use when registering your `ChunkGenerator` codec in `WorldSize.java`

---

## Quick Reference: Key Imports for 1.21.11 Mojang Mappings

```java
// Identifiers (ResourceLocation was renamed in 1.21.11)
import net.minecraft.resources.Identifier;

// Server-side world and player
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.WorldGenRegion;

// World gen
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.minecraft.world.level.biome.BiomeSource;

// Registry
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
```

## Registration Example (WorldSize.java)

```java
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.fabricmc.api.ModInitializer;

public class WorldSize implements ModInitializer {
    public static final String MOD_ID = "worldsize";

    @Override
    public void onInitialize() {
        Registry.register(
            BuiltInRegistries.CHUNK_GENERATOR,
            Identifier.fromNamespaceAndPath(MOD_ID, "torus"),
            TorusChunkGenerator.CODEC
        );
    }
}
```