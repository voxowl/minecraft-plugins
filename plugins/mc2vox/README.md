# VoxelWitch

VoxelWitch is a plugin that allows you to export portions of your Minecraft world into [Magicavoxel](https://ephtracy.github.io) `.vox` files (compatible with WorldEdit).

## How to install

⚠️ Make sure that you are using the Java version of Minecraft.
⚠️ Your Minecraft server has to be a Bukkit server such as [Spigot](https://spigotmc.org) or [Paper](https://papermc.io) (Paper is recommended over Spigot)

1. Put VoxelWitch jarfile in `/plugins` folder, you can add [WorldEdit](https://dev.bukkit.org/projects/worldedit/files) plugin if you want
2. Start your server
3. Enjoy! ✨

## How to use

#### Choose an area, like this bridge:

<img width="498" alt="Screen Shot 2021-05-19 at 22 52 31" src="https://user-images.githubusercontent.com/6775074/118882869-e9dc6c00-b8f4-11eb-9f59-eb42cdd861b4.png">

#### Select the box you want to export using the selection wand (by default, the wand is leather):
  - Holding the selection wand in the main hand:
    - Left click for the first corner block
    - Right click for the second corner block

<img width="332" alt="Screen Shot 2021-05-19 at 22 57 54" src="https://user-images.githubusercontent.com/6775074/118883592-b221f400-b8f5-11eb-82b6-f0a3aa41f779.png">

💡 For the upper part of your selection, do not hesitate to create a staircase or a ladder with dirt to reach the desired height.

💡 This plugin can use another plugin called **WorldEdit**, and that plugin provides other ways to select a volume. You can check [this link](https://worldedit.enginehub.org/en/latest/usage/regions/selections/) to learn more about this.

#### Export your `.vox` file

- Use this command: `/vw export <filename>`
- You can find your `.vox` file in the `voxs` folder in plugin's config folder.

#### Upload your `.vox` file

- Use this command: `/vw upload <filename>`
- Click on the link in the dialog box to download the `.vox` file.
 
The `.vox` file can be opened in with [Magicavoxel](https://ephtracy.github.io):

<img width="567" alt="Screen Shot 2021-05-19 at 23 15 26" src="https://user-images.githubusercontent.com/6775074/118885504-278ec400-b8f8-11eb-967c-d96b74e4f1ce.png">

It can also be imported in [Particubes](https://particubes.com) and other softwares supporting `.vox`. 🙂

## Commands

* `/voxelwitch` - Base command

  * Permission : `voxelwitch.cmd` - This permission is needed for the next commands
  * Aliases : `/vwitch`, `/vw`

* `/voxelwitch export <filename>` - Export region to .vox file in `/plugins/VoxelWitch/voxs` folder

  * Permission : `voxelwitch.export`
      
* `/voxelwitch upload <filename>` - Export region to .vox file and get a url link to download the file (validity : 1 day)

  * Permission : `voxelwitch.upload`

#### Other permissions

 * `voxelwitch.wand` - Permission to use the selection wand (standalone mode)

## Config files

* `colors.json` - contains the palette with color and materials and block map.
* `config.yml`  - contains the settings of VoxelWitch.
  * `donjon-hostname` - VoxelDonjon hostname (use HTTPS)
  * `worldedit` - If set to false, the plugin will always run in the standalone mode
  * `item-wand` - Change the item for the VoxelWitch's wand (only in standalone mode), you need to use [material](https://hub.spigotmc.org/javadocs/bukkit/org/bukkit/Material.html) names

## Examples

![exportvoxelwitch](https://user-images.githubusercontent.com/33163837/119567926-33bdca00-bdad-11eb-97e8-e84016cd0701.png)

