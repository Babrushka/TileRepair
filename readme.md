### install through compiled binaries

1. download and install https://github.com/Brov3r/Avrix

1.1 download Avrix-Core-1.5.2.jar from releases page and place it to "Project Zomboid" root folder

1.2 same thing with AvrixLauncher-Client-NoSteam.bat

1.3 configure AvrixLauncher-Client-NoSteam.bat if needed.

1.4 downloade sources from https://github.com/Babrushka/TileRepair

1.5 copy TileRepair/build/libs/tilerepair-v*.jar to "Project Zomboid"/plugins folder

1.6 run AvrixLauncher-Client-NoSteam.bat to start the game

### build from source

#### pre requirements

1.1 `git clone --branch v1.5.2 https://github.com/Brov3r/Avrix` 

1.2 `cd avrix` 

1.3 build avrix and all dependencies using original Brov3r's guide.

#### copy and build plugin

2.1 `cd avrix && mkdir plugins && cd plugins`

2.2 git clone https://github.com/Babrushka/TileRepair

2.3 gradle build

### install files and launch game

3.1 copy avrix/plugins/TileRepair/build/libs/tilerepair-v*.jar to "Project Zomboid"/plugins folder

3.2 copy avrix/build/libs/Avrix-1.5.2.jar to "Project Zomboid" root folder

3.3 copy avrix/scripts/AvrixLauncher-Client-NoSteam.bat "Project Zomboid" root folder

3.4 configure if needed AvrixLauncher-Client-NoSteam.bat and run it to launch the game.


