# Unofficial Naturalist 1.21.1 Fabric Migration

This is a migrated version of Naturalist from 1.20.1 to 1.21.1, exclusively for **Fabric** (sans the licensed assets).
Additionally, some features/compatibility have been removed during the process.

### Migration Changes
- Updated from Minecraft 1.20.1 to 1.21.1
- Removed Zebra due to bad animations
- Removed Ducks for compatibility with "Untitled Duck Mod"
- Animal food preferences are now hard-coded instead of using data pack tags due to migration difficulties

## Original Credits

**Original Mod:** Naturalist by **Starfish Studios**
- **Homepage:** https://www.curseforge.com/minecraft/mc-mods/naturalist
- **Issues:** https://github.com/starfish-studios/Naturalist/issues
- **Copyright:** © 2022 Starfish Studios, Inc.

## Dependencies

- **Minecraft:** 1.21.1+
- **Fabric Loader:** 0.16.14+
- **GeckoLib:** 4.7.6+
- **MidnightLib:** 1.4.1+ (Fabric)

## 📦 Required Assets

**⚠️ IMPORTANT:** This repository does **NOT** include the required assets (textures, models, animations, sounds, data) due to licensing restrictions.

### How to Obtain Assets

1. **Download** the original Naturalist mod for Minecraft 1.20.1:
   - https://github.com/starfish-studios/Naturalist/

2. **Extract** the following directories from the 1.20.1 mod:
   ```
   Naturalist-Arch-1.20.1/common/src/main/resources/
   Naturalist-Arch-1.20.1/fabric/src/main/resources/
   ```

3. **Copy** these directories to replace the empty ones in this project:
   ```
   common/src/main/resources/     ← Copy content here
   fabric/src/main/resources/     ← Copy content here
   ```

### Asset Compatibility

This 1.21.1 version includes **flexible animation loading** that supports:
- **Old format:** `.rp_anim.json` files (from 1.20.1)
- **New format:** `.animation.json` files  

The mod will automatically detect and use whichever format is available.

## Building

**Prerequisites:** Ensure you have copied the required assets as described above.

```bash
cd /path/to/directory/
./gradlew build
```

The built mod jar will be in `fabric/build/libs/`.

### Without Assets
If you try to build without assets, the mod will compile but **will not function properly** in-game (missing textures, models, animations, etc.).

## License

This migration is licensed under the MIT License, as no licensed assets are included in this repository.

- **Code:** MIT License © 2022 Starfish Studios, Inc. (original) & contributors (migration)
- **Assets:** Not included - must be obtained separately from official sources

See the [LICENSE](LICENSE) file for full details.

---

*This migration was performed independently and is not officially endorsed by Starfish Studios.*