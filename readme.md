## Features
This plugin is intended to use for repairing tiles/squares/chunks destroyed by griefers or even players.

#### Launch in game

After installing all files launch game through avrix launcer, sign in to server/singleplayer, and press <em>delete</em>. It will launch described above procedures (all in one, but each procedure can be disabled).
Hotkey can be configured in __config.yml__ created automatically after 1st lauch of game with plugin.

---

### Delete unwanted tiles, even not native objects created programmaticaly.

Plugin allows to delete unwanted objects from around the player in specified radius (__radius__).

Deleted tiles must be configured via 3 fields:
    - __objectSpriteName__ in java is `object.spriteName` field 
    - __spriteSpriteName__ `object.sprite.name` or `object.getSprite().getName()`
    - __spriteID__.

Procedure can enabled or disabled in config file.

### Restore floors

Restore floor function attempts to read tiledefinitions from native PZ *.lotpack files and apply them to all squares in player chunk (10x10 space around player, linked to gamegrid, not relative to player). 
Algorigthm of restoring readind datafiles and applying changes is simillar to one, used when new unvisited chunks are created. Not a secret, it was copied and a bit mididied from PZ source code.

The only specified field is __floorName__: same as object.sprite.name, can be specified to apply changes only on specified floors.

> [!CAUTION] 
> If in game is no floor (no squares with `solidfloor/diamondfloor` flags, __floorName__ object on square is `null`, etc) procedure crashes!

### Restore other objecs

For user the result of this procedure looks same as Restore floors, but this procedure is a bit complicated in comparsion to previous one. It creates all default objects in chunk (except floors), compares them with existing, delete copies, and tries to sync unique created objects to server, skipping sync on existing objects. This algo can lead to unexpected behaviour on complex chuncks with many user objects. 
> [!TIP] 
> To achieve best results it is recommended to clear chunk from user objects placed on squares, where default one's can be spawned, close all doors, etc.

> [!IMPORTANT] 
> All Described above procedures only works on level 0. Probably feature versions will allow to use other values.
***
## Install through compiled binaries

1. Download and install [Avrix Core](https://github.com/Brov3r/Avrix)

    1. Download [Avrix-Core-1.5.2.jar](https://github.com/Brov3r/Avrix/releases/download/v1.5.2/Avrix-Core-1.5.2.jar) from releases page and put it to "Project Zomboid" root folder

    2. Download [AvrixLauncher-Client-NoSteam.bat](https://github.com/Brov3r/Avrix/releases/download/v1.5.2/AvrixLauncher-Client-NoSteam.bat) from releases page and put it to "Project Zomboid" root folder

        Configure AvrixLauncher-Client-NoSteam.bat if needed.

2. Download sources from [TileRepair](https://github.com/Babrushka/TileRepair)

    Copy TileRepair/build/libs/tilerepair-v*.jar to "Project Zomboid"/plugins folder

3. Run AvrixLauncher-Client-NoSteam.bat to start the game

***

## Build from source

#### Prerequirements:

```
git clone --branch v1.5.2 https://github.com/Brov3r/Avrix
cd avrix
```
__Build__ avrix and all dependencies using original Brov3r's guide.

### Copy and build plugin:

```
cd avrix && mkdir plugins && cd plugins
git clone https://github.com/Babrushka/TileRepair
cd TileRepair
gradle build
```

> [!IMPORTANT] 
> Building from sources requires JDK-17 for compability with the game!

