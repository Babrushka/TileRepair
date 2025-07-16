package com.tilerepair;

import java.io.File;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;

import gnu.trove.list.array.TIntArrayList;
import zombie.characters.IsoPlayer;
import zombie.core.network.ByteBufferWriter;
import zombie.core.properties.PropertyContainer;
import zombie.debug.DebugLog;
import zombie.iso.CellLoader;
import zombie.iso.IsoCell;
import zombie.iso.IsoChunk;
import zombie.iso.IsoGridSquare;
import zombie.iso.IsoLot;
import zombie.iso.IsoObject;
import zombie.iso.LotHeader;
import zombie.iso.SpriteDetails.IsoFlagType;
import zombie.iso.sprite.IsoSprite;
import zombie.iso.sprite.IsoSpriteInstance;
import zombie.iso.sprite.IsoSpriteManager;
import zombie.network.GameClient;
import zombie.network.PacketTypes;
import zombie.util.list.PZArrayList;

public class TileRepair {
    private int radius;

    private int x, y, wx, wy;
    private IsoPlayer player;
    private IsoGridSquare gsquare;
    private IsoChunk chunk;
    private IsoCell cell;

    private IsoLot lot = null;
    private ArrayList<IsoSprite>[][][] objectSprites, floorOverlays;
    private String[][][] floorSpritesStrings;
    private ArrayList<String>[][][] objectSpritesStrings, floorOverlaysStrings;
    private IsoSprite[][][] floorSprites;

    List<Object> tilesToRemoveList;
    List<Object> floorList;
    int[][] levels;

    private HashSet<String> missingTiles = null;
    private Field finfo = null, fm_offsetInData = null, fm_data = null;

    public TileRepair(int _radius, List<Object>[] lists, String[] _levels) {
        this.radius = _radius;
        levels = new int[3][];
        for (int i = 0; i < 3;i++) {
            String[] levelsstrings = _levels[i].split(",");
            levels[i] = new int[levelsstrings.length];
            int j=0;
            for (String string : levelsstrings) {
                levels[i][j++] = Integer.parseInt(string);
            }
        }
        

        this.floorList = lists[0];
        this.tilesToRemoveList = lists[1];

        this.floorSprites = new IsoSprite[10][10][8];
        this.floorOverlays = new ArrayList[10][10][8];
        this.objectSprites = new ArrayList[10][10][8];
        this.floorSpritesStrings = new String[10][10][8];
        this.objectSpritesStrings = new ArrayList[10][10][8];
        this.floorOverlaysStrings = new ArrayList[10][10][8];
    }

    public void Init(){
        this.player = IsoPlayer.getInstance();
        this.gsquare = player.getSquare();
        this.x = gsquare.getX();
        this.y = gsquare.getY();
        this.cell = player.getCell();
        
        Field fmissingTiles = null;
        try {
            fmissingTiles = CellLoader.class.getDeclaredField("missingTiles");
            finfo = IsoLot.class.getDeclaredField("info");
            fm_data = IsoLot.class.getDeclaredField("m_data");
            fm_offsetInData = IsoLot.class.getDeclaredField("m_offsetInData");
        } catch (NoSuchFieldException e) {
        }
        fmissingTiles.setAccessible(true);
        finfo.setAccessible(true);
        fm_offsetInData.setAccessible(true);
        fm_data.setAccessible(true);
        try {
            missingTiles = (HashSet<String>)fmissingTiles.get(CellLoader.class);
        } catch (IllegalAccessException e) {
        }
        


    }

    private void sendPacketDestroyObjectonSquare(IsoGridSquare square, IsoObject object) {
        ByteBufferWriter byteBufferWriter = GameClient.connection.startPacket();
        PacketTypes.PacketType.SledgehammerDestroy.doPacket(byteBufferWriter);
        byteBufferWriter.putInt(square.getX());
        byteBufferWriter.putInt(square.getY());
        byteBufferWriter.putInt(square.getZ());
        byteBufferWriter.putInt(square.getObjects().indexOf(object));
        PacketTypes.PacketType.SledgehammerDestroy.send(GameClient.connection);
    }

    private boolean checkFloor(IsoObject obj) {
        PropertyContainer props = obj.sprite.getProperties();
        if (props.Is(IsoFlagType.solidfloor) && props.Is(IsoFlagType.diamondFloor)
                                        && !props.Is(IsoFlagType.transparentFloor)){
            return true;
        } else{
            return false;
        }
    }

    private boolean compareObjects(IsoObject object, List<Object> List) {
        if (object==null)
            return false;
        if (object.sprite == null)
            return false;
        for (Object obj : List) {
            if (obj.equals("all") || obj.equals("All") || obj.equals("ALL")) {
                return true;
            } else {
                LinkedHashMap hobj = (LinkedHashMap) obj;
                String objectSpritename = object.getSpriteName();
                if (objectSpritename == null) {
                    objectSpritename = "";
                }
                String spriteSpriteName = object.sprite.getName();
                if (spriteSpriteName == null) {
                    spriteSpriteName = "";
                }
                int spriteID = object.sprite.getID();

                String _spriteSpriteName = (String) hobj.get("sprite.name");
                String _objectSpritename = (String) hobj.get("object.spritename");
                Integer _spriteID = (Integer) hobj.get("spriteID");
                boolean id = false, nsprite = false, nobject = false;
                if (_spriteSpriteName != null) {
                    if (_spriteSpriteName.equals(spriteSpriteName)) {
                        nsprite = true;
                    }
                } else {
                    nsprite = true;
                }
                if (_objectSpritename != null) {
                    if (_objectSpritename.equals(objectSpritename)) {
                        nobject = true;
                    }
                } else {
                    nobject = true;
                }
                if (_spriteID != null) {
                    if (_spriteID == spriteID) {
                        id = true;
                    }
                } else {
                    id = true;
                }
                if (id && nsprite && nobject) {
                    return true;
                }
            }
            
        }
        return false;
    }

    public void clearLot(){
        IsoLot.put(lot);
    }    
    public void readDataFiles() {
        chunk = player.getChunk();
        wx = chunk.wx;
        wy = chunk.wy;
        String datafilename = "world_" + wx / 30 + "_" + wy / 30 + ".lotpack";
        if (!IsoLot.InfoFileNames.containsKey(datafilename)) {
            DebugLog.log("LoadCellBinaryChunk: NO SUCH LOT " + datafilename);
        } else {
            File file = new File(IsoLot.InfoFileNames.get(datafilename));
            if (file.exists()) {
                
                this.lot = IsoLot.get(wx / 30, wy / 30, wx, wy, chunk);
                //cell.PlaceLot(lot, 0, 0, 0, chunk, wx, wy);
                this.ReadDefaultSprites(lot);
            }
        }
    }

    private void ReadDefaultSprites(IsoLot lot) {
        LotHeader lotinfo = null;
        int[] m_offsetInData = null;
        TIntArrayList m_data = null;
        Field ftilesUsed = null;
        try {
            lotinfo = (LotHeader) finfo.get(lot);
            m_offsetInData = (int[]) fm_offsetInData.get(lot);
            m_data = (TIntArrayList) fm_data.get(lot);
        } catch (IllegalAccessException e) {
        }
        try {
            ftilesUsed = lotinfo.getClass().getDeclaredField("tilesUsed");
        } catch (NoSuchFieldException e) {
        }
        ftilesUsed.setAccessible(true);
        ArrayList<String> tilesUsed = null;
        try {
            tilesUsed = (ArrayList<String>) ftilesUsed.get(lotinfo);
        } catch (IllegalAccessException e) {
        }

        int lotheight = Math.min(lotinfo.levels, 8);

        for (int i = 0; i < 10; ++i) {
            for (int j = 0; j < 10; ++j) {
                for (int h = 0; h < lotheight; ++h) {
                    this.floorOverlays[i][j][h] = new ArrayList<IsoSprite>();
                    this.objectSprites[i][j][h] = new ArrayList<IsoSprite>();
                    this.objectSpritesStrings[i][j][h] = new ArrayList<String>();
                    this.floorOverlaysStrings[i][j][h] = new ArrayList<String>();
                    this.floorSpritesStrings[i][j][h] = null;
                    int addr = m_offsetInData[i + j * 10 + h * 100];
                    if (addr != -1) {
                        int tilescount = m_data.getQuick(addr);
                        if (tilescount > 0) {
                            for (int spriteindex = 0; spriteindex < tilescount; spriteindex++) {
                                String string = tilesUsed.get(m_data.get(addr + 1 + spriteindex));
                                if (!lotinfo.bFixed2x) {
                                    string = IsoChunk.Fix2x(string);
                                }
                                IsoSprite sprite = IsoSpriteManager.instance.NamedMap.get(string);
                                PropertyContainer props = sprite.getProperties();
                                if (props.Is(IsoFlagType.solidfloor) && props.Is(IsoFlagType.diamondFloor)
                                        && !props.Is(IsoFlagType.transparentFloor)) {
                                    this.floorSprites[i][j][h] = sprite;
                                    this.floorSpritesStrings[i][j][h] = string;
                                    this.floorOverlays[i][j][h].clear();
                                    this.floorOverlaysStrings[i][j][h].clear();
                                    this.objectSprites[i][j][h].clear();
                                    //this.objectSpritesStrings[i][j][h].clear();
                                } else if (sprite.getProperties().Is(IsoFlagType.FloorOverlay)) {
                                    this.floorOverlays[i][j][h].add(sprite);
                                    this.floorOverlaysStrings[i][j][h].add(string);
                                } else {
                                    this.objectSprites[i][j][h].add(sprite);
                                    this.objectSpritesStrings[i][j][h].add(string);
                                }
                            }
                        }
                    }
                }
            }

        }

    }

    
    public void restoreFloors() {
        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 10; j++) {
                for (int h : levels[1]) {
                    IsoGridSquare _square = cell.getGridSquare(wx * 10 + i, wy * 10 + j, h);
                    if (_square!=null) restoreFloor(i, j, h);
                }
         
            }
        }
    }

    private void restoreFloor(int i, int j, int h) {
        IsoGridSquare square = cell.getGridSquare(wx * 10 + i, wy * 10 + j, h);
        IsoObject object = square.getFloor();
        if (object == null) {
            if (this.floorSpritesStrings[i][j][h] != null) {
                object = square.addFloor(this.floorSpritesStrings[i][j][h]);
                if (zombie.network.GameClient.connection != null)
                    object.transmitCompleteItemToServer();
            }
            
        }
        if (compareObjects(object, floorList)) {
            object.clearAttachedAnimSprite();
            object.setSprite(this.floorSprites[i][j][h]);
            int size = this.floorOverlays[i][j][h].size();

            for (int over = 0; over < size; over++) {
                IsoSprite _overlaySprite = this.floorOverlays[i][j][h].get(over);
                if (object.AttachedAnimSprite == null) {
                    object.AttachedAnimSprite = new ArrayList<IsoSpriteInstance>(4);
                }
                object.AttachedAnimSprite.add(IsoSpriteInstance.get(_overlaySprite));

            }
            if (zombie.network.GameClient.connection != null)
                object.transmitUpdatedSpriteToServer();

        }
    }
    
    private void restoreObjectOnSquare(int i, int j, int h) {
        IsoGridSquare square = cell.getGridSquare(wx * 10 + i, wy * 10 + j, h);
        int oldObjectsize = square.getObjects().size();
        
        ArrayList<IsoObject> oldObjects = new ArrayList<IsoObject>();
        for (int o = 0; o < oldObjectsize; o++) {
            oldObjects.add(square.getObjects().get(o));
        }
        int defaultObjectsCount = objectSprites[i][j][h].size();
        for (int s = 0; s < defaultObjectsCount; s++) {
            IsoSprite sprite = objectSprites[i][j][h].get(s);
            String string = objectSpritesStrings[i][j][h].get(s);
            boolean exist = false;
            for (int o = 0; o < oldObjectsize; o++) {
                IsoSprite spriteOldObj = oldObjects.get(o).getSprite();
                if (sprite.getName().equals(spriteOldObj.getName())) {
                    exist = true;
                }
                if (!(oldObjects.get(o).AttachedAnimSprite == null)) {
                    for (int k = 0; k < oldObjects.get(o).AttachedAnimSprite.size(); k++) {
                        String oldObjStr = oldObjects.get(o).AttachedAnimSprite.get(k).getName();
                        if (sprite.getName().equals(oldObjStr)) {
                            exist = true;
                        }
                    }
                }
            }
            if (!exist) {
                CellLoader.DoTileObjectCreation(sprite, sprite.getType(), square, cell, wx * 10 + i,
                        wy * 10 + j, h, string);
            }
        }
        PZArrayList<IsoObject> newObjects = square.getObjects();
        for (int n = newObjects.size() - 1; n >= 0; n--) {
            String brokenSprite = newObjects.get(n).sprite.name;
            if (missingTiles.contains(brokenSprite)) {
                square.RemoveTileObject(newObjects.get(n));
            }
        }
        for (int n = 0; n < newObjects.size(); n++) {
            boolean objectsEqual = false;
            IsoObject o1 = newObjects.get(n);
            for (int o = 0; o < oldObjects.size(); o++) {
                IsoObject o2 = oldObjects.get(o);
                if (o2.equals(o1)) {
                    objectsEqual = true;
                }

            }
            if (!objectsEqual) {
                if (zombie.network.GameClient.connection != null)
                    o1.transmitCompleteItemToServer();
            }
        }
    }

    public void restoreObjects() {
        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 10; j++) {
                for (int h : levels[2]) {
                    IsoGridSquare _square = cell.getGridSquare(wx * 10 + i, wy * 10 + j, h);
                    if (_square != null) {
                        restoreObjectOnSquare(i, j, h);
                    }
                }
            }
        }
    }

    public void deleteTiles() {
        for (int i = (x - radius); i <= (x + radius); i++) {
            for (int j = y - radius; j <= (y + radius); j++) {
                for (int h : levels[0]) {
                    IsoGridSquare square = cell.getGridSquare(i, j, h);
                    if (square != null) {
                        PZArrayList<IsoObject> objects = square.getObjects();
                        for (int k = objects.size() - 1; k >= 0; k--) {
                            IsoObject object = objects.get(k);
                            IsoObject floor = square.getFloor();
                            if (!object.equals(floor)) {
                                if (compareObjects(object, this.tilesToRemoveList)) 
                                {
                                    if (zombie.network.GameClient.connection != null)
                                        sendPacketDestroyObjectonSquare(square, object);
                                    square.RemoveTileObject(object);
                                }
                            }

                        }
                    }
                }
            }
        }
    }    
}
