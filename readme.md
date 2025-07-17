## Features
This plugin is intended to use for repairing tiles/squares/chunks destroyed by griefers or even players.

#### Launch in game

After installing all files launch game through avrix launcer, sign in to server/singleplayer, and press specified in __config.yml__ hotkey. It will launch described below procedures.
__config.yml__ is created automatically after 1st lauch of game with plugin (`PZ root/plugins/tilerepair-client-plugin/config.yml`).
Please touch __config.yml__  carefully, wrong definitions can harm a lot.

> [!WARNING]
> #### Applying any changes in multiplayer mode requires stable internet connection. To achieve best results it is recommended to aplly changes one by one, leaving the chunks after any actions, and re entering them to confirm the changes are synced to the server. Overwise async can lead to destroing or dupe wrong objects!!!


---

### Delete unwanted tiles, even not native objects created programmaticaly.

Plugin allows to delete unwanted objects from around the player in specified __radius__.

Deleted tiles must be configured via one or more of 3 fields:
* __object.spritename__ in java is `object.spriteName` field 
* __sprite.name__ `object.sprite.name` or `object.getSprite().getName()`
* __spriteID__ - regular one spriteID.

Delete function skips trying to delete "main" floors, recieved by square.getFloor() in all cases, but can delete overlaying objects;
Plugin support multiple object definitions through lists, for more details see __config.yml__


### Restore floors

Restore floor function attempts to read tiledefinitions from native PZ *.lotpack files and apply them to all squares in player chunk (10x10 space around player, linked to gamegrid, not relative to player). 
Algorigthm of restoring tiles, reading datafiles and applying changes is simillar to one, used when new unvisited chunks are created. Not a secret, it was copied and a bit mididied from PZ source code.

__floorList__ can be specified same way described above to restore floors only on specified tiles;
> [!IMPORTANT]
> If game contains multiple floors, overlaying default one, then the changes will be applied only to first object recieved by square.getFloor() native procedure and can be unseen by user because applied object is laying below another visible one.

### Restore other objecs

For user the result of this procedure looks same as Restore floors, but this procedure is a bit complicated in comparsion to previous one. It creates missing default objects in chunk (except floors), and tries to sync unique created objects to server, skipping sync on existing objects. This algo can lead to unexpected behaviour on complex chuncks with many user objects. 
> [!TIP] 
> To achieve best results it is recommended to clear chunk from user objects placed on squares, where default one's can be spawned, close all doors, etc.

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

