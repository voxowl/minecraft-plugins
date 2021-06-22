# VoxelWitch

VoxelWitch is a plugin that allows you to export portions of your Minecraft world into [Magicavoxel](https://ephtracy.github.io) `.vox` files using [WorldEdit](https://dev.bukkit.org/projects/worldedit) selection.

## How to install

⚠️ Make sure that you are using the Java version of Minecraft.
⚠️ Your Minecraft server has to be a Bukkit server such as [Spigot](https://spigotmc.org) or [Paper](https://papermc.io) (Paper is recommended over Spigot)

1. Put [WorldEdit](https://dev.bukkit.org/projects/worldedit/files) and VoxelWitch jarfiles in `/plugins` folder
2. Start your server
3. Enjoy! ✨

## How to use

#### Choose an area, like this bridge:

<img width="498" alt="Screen Shot 2021-05-19 at 22 52 31" src="https://user-images.githubusercontent.com/6775074/118882869-e9dc6c00-b8f4-11eb-9f59-eb42cdd861b4.png">

#### Select the box you want to export using the wooden axe:
  - Left click for the first corner block
  - Right click for the second corner block

<img width="332" alt="Screen Shot 2021-05-19 at 22 57 54" src="https://user-images.githubusercontent.com/6775074/118883592-b221f400-b8f5-11eb-82b6-f0a3aa41f779.png">

 💡 For the upper part of your selection, do not hesitate to create a staircase or a ladder with dirt to reach the desired height.
 
 ⚠️ Possible issue: if the wooden axe doesn't work, take it in your hand and use the `/addwand` command (`/remwand` to detach the function).

 👉 It is  also possible to set the 2 box selection corners using `/pos1` and `/pos2` commands while standing on the chosen blocks.

#### Upload your `.vox` file

- Use this command: `/vw upload <filename>`
- Click on the link in the dialog box to download the `.vox` file.
 
The `.vox` file can be opened in with [Magicavoxel](https://ephtracy.github.io):

<img width="567" alt="Screen Shot 2021-05-19 at 23 15 26" src="https://user-images.githubusercontent.com/6775074/118885504-278ec400-b8f8-11eb-967c-d96b74e4f1ce.png">

It can also be imported in [Particubes](https://particubes.com) and other softwares supporting `.vox`. 🙂

## Commands

* `/voxelwitch` - Base command

  * Permission : `voxelwitch.cmd`
  * Aliases : `/vwitch`, `/vw`

* `/voxelwitch export <filename>` - Export region to .vox file in `/plugins/VoxelWitch/voxs` folder

  * Permission : `voxelwitch.export`
      
* `/voxelwitch upload <filename>` - Export region to .vox file and get a url link to download the file (validity : 1 day)

  * Permission : `voxelwitch.upload`

## Config files

* `colors.json` - contains the palette with color and materials and block map.
* `config.yml`  - contains the settings of VoxelWitch.

## Examples

![exportvoxelwitch](https://user-images.githubusercontent.com/33163837/119567926-33bdca00-bdad-11eb-97e8-e84016cd0701.png)

