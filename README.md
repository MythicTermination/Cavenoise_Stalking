# :pick: Cave Dweller (cavenoise)

This repository contains a decompilation of the "Cave Dweller" Minecraft mod by [Gargin](https://www.youtube.com/@Gargin).
It should remain functionally equivalent to the source code as to provide a foundational point for forks, ports, and backports.

Both this and the source are licensed under [MIT](https://opensource.org/license/mit).

**Disclaimer: I am not affiliated with Gargin in any way.**

## Mappings

This project uses the [Parchment](https://parchmentmc.org) mappings rather than standard Mojang mappings (Mojmap).
While Mojmap only provides field, method, and class names, Parchment also adds documentation and parameter names on top of Mojmap.

Forge provides a word of caution when using unofficial mappings as they may sometimes not work. If you wish not to use Parchment mappings, you can revert to standard Mojmap via the `build.gradle` file:

```groovy
mappings channel: 'official', version: '1.19.3'
```

Considerations:

- Parchment is usually one version behind Mojang mappings; however, it is possible to use earlier versions of Parchment with newer versions of Minecraft.
- Mojang mappings do not exist prior to 1.14. You will want to use MCP mappings at that point.
- MCP mappings do not exist past 1.17. If possible, you should use Mojang/Parchment mappings anyway as they are more complete than those provided by MCP.
- Yarn, the mappings used by Fabric, are less restrictive than alternatives. The caveat is that you *will* have more trouble finding support online.

Usage of Mojang mappings must additionally adhere to this license:

```
(c) 2020 Microsoft Corporation. These mappings are provided "as-is" and you bear the risk of using them.
You may copy and use the mappings for development purposes, but you may not redistribute the mappings complete and unmodified.
Microsoft makes no warranties, express or implied, with respect to the mappings provided here.
Use and modification of this document or the source code (in any form) of Minecraft: Java Edition is governed by the Minecraft End User License Agreement available at https://account.mojang.com/documents/minecraft_eula.
```

## Process

In order to actually create this decompilation, I followed these steps:

- Creating a new Forge project.
- Importing the mod as a local `implementation fg.deobf` dependency.
- Retrieving the deobfuscated jar from ForgeGradle's cache.
- Decompiling the jar with [Vineflower](https://github.com/Vineflower/vineflower), no additional settings.
- Creating another new Forge project and placing the decompiled code and extracted resources into the correct directories under `src/main`.
- Added GeckoLib 4.0.4 as a dependency.

## Contributing

I will not accept contributions for code that changes the behaviour (including bug fixes), metadata, or signatures of the mod.
Please contribute these changes to a fork, either yours or someone else's.
