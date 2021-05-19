# VoxelWitch

VoxelWitch is a plugin that allows you to export portions of your Minecraft world into Magicavoxel `.vox` files using WorldEdit selection.

## How to install

1. Put [WorldEdit](https://dev.bukkit.org/projects/worldedit/files) and VoxelWitch jarfiles in `/plugins` folder
2. Start your server
3. Enjoy !

## How to use

1. Make a region with WorldEdit (127x127x127 blocks max)
2. Export your .vox file

### Commands

Base permission : `voxelwitch.cmd`

* `/voxelwitch export <filename>` - Export region to .vox file in `/plugins/VoxelWitch/voxs` folder

  * Permission : `voxelwitch.export`
      
* `/voxelwitch upload <filename>` - Export region to .vox file and get a url link to download the file (validity : 1 day)

  * Permission : `voxelwitch.upload`

### Configs

There is two config files :
* `colors.json` - contains the palette with color and materials and block map.
* `config.yml`  - contains the settings of VoxelWitch.
