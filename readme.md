### install through compiled binaries

1. Download and install https://github.com/Brov3r/Avrix

1.1. Download Avrix-Core-1.5.2.jar from releases page and put it to "Project Zomboid" root folder

1.2. Download AvrixLauncher-Client-NoSteam.bat from releases page and put it to "Project Zomboid" root folder

1.2.2. Configure AvrixLauncher-Client-NoSteam.bat if needed.

2. Download sources from https://github.com/Babrushka/TileRepair

2.1. Copy TileRepair/build/libs/tilerepair-v*.jar to "Project Zomboid"/plugins folder

3. Run AvrixLauncher-Client-NoSteam.bat to start the game

### build from source

#### pre requirements

1.1. `git clone --branch v1.5.2 https://github.com/Brov3r/Avrix` 

1.2. `cd avrix` 

1.3 Build avrix and all dependencies using original Brov3r's guide.

#### copy and build plugin

2.1. `cd avrix && mkdir plugins && cd plugins`

2.2. git clone https://github.com/Babrushka/TileRepair

2.3. gradle build

### install files and launch game

3.1. copy avrix/plugins/TileRepair/build/libs/tilerepair-v*.jar to "Project Zomboid"/plugins folder

3.2. copy avrix/build/libs/Avrix-1.5.2.jar to "Project Zomboid" root folder

3.3. copy avrix/scripts/AvrixLauncher-Client-NoSteam.bat "Project Zomboid" root folder

3.4. configure if needed AvrixLauncher-Client-NoSteam.bat and run it to launch the game.


