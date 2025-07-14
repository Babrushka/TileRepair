package com.avrix.tilerepair;

import com.avrix.events.OnKeyPressedEvent;

import java.io.File;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashSet;
import com.avrix.ui.NanoContext;

import gnu.trove.list.array.TIntArrayList;
import zombie.characters.IsoPlayer;
import zombie.core.network.ByteBufferWriter;
import zombie.core.properties.PropertyContainer;
import zombie.iso.CellLoader;
import zombie.iso.IsoCell;
import zombie.iso.IsoChunk;
import zombie.iso.IsoGridSquare;
import zombie.iso.IsoObject;
import zombie.iso.LotHeader;
import zombie.iso.SpriteDetails.IsoFlagType;
import zombie.util.list.PZArrayList;
import zombie.iso.sprite.IsoSprite;
import zombie.iso.sprite.IsoSpriteInstance;
import zombie.iso.sprite.IsoSpriteManager;
import zombie.network.GameClient;
import zombie.network.PacketTypes;

import zombie.debug.DebugLog;
import zombie.iso.IsoLot;


/**
 * Draw HUD
 */
public class KeyHandler extends OnKeyPressedEvent {
    private int radius;
    private int hotkey;
    private int spriteID;
    private int x, y, wx, wy;
    private IsoSprite[][] floorSprites;
    private IsoPlayer player;
    private IsoGridSquare gsquare;
    private IsoChunk chunk;
    private IsoCell cell;
    IsoLot lot = null;
    private String floorName, objectSpriteName, spriteSpriteName;
    boolean restoreFloors, deleteTiles, restoreOtherObjects;
    ArrayList<IsoSprite>[][] objectSprites, floorOverlays;
    ArrayList<String>[][] objectSpritesStrings, floorOverlaysStrings;

    public KeyHandler(int _radius, int _key, String _floorName, boolean _restoreFloors, boolean _restoreOtherObjects,
            boolean _deleteTiles, String _spriteSpriteName, String _objectSpriteName, int _spriteID) {
        this.spriteSpriteName = _spriteSpriteName;
        this.objectSpriteName = _objectSpriteName;
        this.spriteID = _spriteID;
        this.radius = _radius;
        this.floorName = _floorName;
        this.hotkey = _key;
        this.floorSprites = new IsoSprite[10][10];
        this.floorOverlays = new ArrayList[10][10];
        this.objectSprites = new ArrayList[10][10];
        this.objectSpritesStrings = new ArrayList[10][10];
        this.floorOverlaysStrings = new ArrayList[10][10];
        this.restoreFloors = _restoreFloors;
        this.restoreOtherObjects = _restoreOtherObjects;
        this.deleteTiles = _deleteTiles;

    }

    private void sendPacketDestroyObjectonSquare(IsoGridSquare square, IsoObject object) {
        ByteBufferWriter byteBufferWriter = GameClient.connection.startPacket();
        PacketTypes.PacketType.SledgehammerDestroy.doPacket(byteBufferWriter);
        byteBufferWriter.putInt(square.getX());
        byteBufferWriter.putInt(square.getY());
        //asdsdgg
        byteBufferWriter.putInt(square.getZ());
        byteBufferWriter.putInt(square.getObjects().indexOf(object));
        PacketTypes.PacketType.SledgehammerDestroy.send(GameClient.connection);
    }

    

    public void ReadDefaultSprites(IsoLot lot) {

        Field finfo = null, fm_offsetInData = null, fm_data = null, ftilesUsed = null;
        ;
        try {
            finfo = lot.getClass().getDeclaredField("info");
            fm_data = lot.getClass().getDeclaredField("m_data");
            fm_offsetInData = lot.getClass().getDeclaredField("m_offsetInData");
        } catch (NoSuchFieldException e) {
        }

        finfo.setAccessible(true);
        fm_offsetInData.setAccessible(true);
        fm_data.setAccessible(true);
        LotHeader lotinfo = null;
        int[] m_offsetInData = null;
        TIntArrayList m_data = null;

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
                this.floorOverlays[i][j] = new ArrayList<IsoSprite>();
                this.objectSprites[i][j] = new ArrayList<IsoSprite>();
                this.objectSpritesStrings[i][j] = new ArrayList<String>();
                this.floorOverlaysStrings[i][j] = new ArrayList<String>();
                for (int h = 0; h < 1; ++h) {
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
                                if (props.Is(IsoFlagType.solidfloor) && props.Is(IsoFlagType.diamondFloor) && !props.Is(IsoFlagType.transparentFloor)) {
                                    this.floorSprites[i][j] = sprite;
                                    this.floorOverlays[i][j].clear();
                                    this.floorOverlaysStrings[i][j].clear();
                                    this.objectSprites[i][j].clear();
                                    this.objectSpritesStrings[i][j].clear();
                                }
                                else if (sprite.getProperties().Is(IsoFlagType.FloorOverlay)) {
                                    this.floorOverlays[i][j].add(sprite);
                                    this.floorOverlaysStrings[i][j].add(string);
                                } else {
                                    this.objectSprites[i][j].add(sprite);
                                    this.objectSpritesStrings[i][j].add(string);
                                }
                            }
                        }
                    }
                }
            }

        }

    }

    private void readDataFiles(IsoPlayer player) {
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

    private void restoreFloor() {
        
        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 10; j++) {
                IsoGridSquare square = cell.getGridSquare(wx * 10 + i, wy * 10 + j, 0);
                IsoObject object = square.getFloor();
                IsoSprite sprite = object.getSprite();
                String str = sprite.getName();
                if (str == null)
                    str = "";
                    if ((floorName.equals("") || floorName.equals(str)) && object!=null) {
                    object.clearAttachedAnimSprite();
                    object.setSprite(this.floorSprites[i][j]);
                    int size = this.floorOverlays[i][j].size();
                    
                    for (int over = 0; over < size; over++) {
                        IsoSprite _overlaySprite = this.floorOverlays[i][j].get(over);
                        String _overlayString = floorOverlaysStrings[i][j].get(over);
                        //CellLoader.DoTileObjectCreation(_overlaySprite, _overlaySprite.getType(), square, cell, wx * 10 + i,
                        //wy * 10 + j, 0,_overlayString);
                        if (object.AttachedAnimSprite == null) {
                            object.AttachedAnimSprite = new ArrayList<IsoSpriteInstance>(4);
                        }
                        object.AttachedAnimSprite.add(IsoSpriteInstance.get(_overlaySprite));
    
                    }
                    //square.FixStackableObjects();
                    //object = square.getFloor();
                    if (zombie.network.GameClient.connection != null)
                        object.transmitUpdatedSpriteToServer();

                }
            
                    
            }
        }
    }

    private void restoreObjects() {
        //cell.PlaceLot(lot, 0, 0, 0, chunk, wx, wy);
        Field fmissingTiles = null;
        
        try {
            fmissingTiles = CellLoader.class.getDeclaredField("missingTiles");
        } catch (NoSuchFieldException e) {
        }
        fmissingTiles.setAccessible(true);
        HashSet<String> missingTiles = null;
        try {
            missingTiles = (HashSet<String>)fmissingTiles.get(CellLoader.class);
        } catch (IllegalAccessException e) {
        }

        for (int i = 0; i < 10; i++) {
            int a = wx * 10 + i;
            for (int j = 0; j < 10; j++) {
                int b = wy * 10 + j;
                IsoGridSquare square = cell.getGridSquare(wx * 10 + i, wy * 10 + j, 0);
                int oldObjectsize = square.getObjects().size();
                ArrayList<IsoObject> oldObjects = new ArrayList<IsoObject>();
                for (int o = 0; o < oldObjectsize; o++) {
                    oldObjects.add(square.getObjects().get(o));
                    //square.RemoveTileObject(oldObjects.get(o));
                }
                int defaultObjectsCount = objectSprites[i][j].size();
                for (int s = 0; s < defaultObjectsCount; s++) {
                    IsoSprite sprite = objectSprites[i][j].get(s);
                    String string = objectSpritesStrings[i][j].get(s);
                    boolean exist = false;
                    for (int o = 0; o < oldObjectsize; o++) {
                        String name1 = oldObjects.get(o).sprite.name == null ? "null" : oldObjects.get(o).sprite.name;
                        if (name1.equals(string)) {
                            exist = true;
                        }
                        if (!(oldObjects.get(o).AttachedAnimSprite == null)) {
                            for (int k = 0; k < oldObjects.get(o).AttachedAnimSprite.size(); k++) {
                                String astring = oldObjects.get(o).AttachedAnimSprite.get(k).getName();
                                if (astring.equals(string)) {
                                    exist = true;
                                }
                            }
                        }
                    }
                    if (!exist) {
                        //sprite.TintMod.set(1, 1, 1, 1);
                        CellLoader.DoTileObjectCreation(sprite, sprite.getType(), square, cell, wx * 10 + i,
                                wy * 10 + j, 0, string);
                    }
                }
                PZArrayList<IsoObject> newObjects = square.getObjects();
                int count = 0;
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
                    //PropertyContainer props = o1.sprite.getProperties();
                    if (!objectsEqual) {
                        if (zombie.network.GameClient.connection != null)
                            o1.transmitCompleteItemToServer();
                    }
                }
                //square.softClear();
                //square.RecalcProperties();
                
                
            }
        }

    }

    private void deleteTiles() {
        for (int i = (x - radius); i <= (x + radius); i++) {
            for (int j = y - radius; j <= (y + radius); j++) {
                IsoGridSquare square = cell.getGridSquare(i, j, 0);
                PZArrayList<IsoObject> objects = square.getObjects();
                for (int k = objects.size() - 1; k >= 0; k--) {
                    IsoObject object = objects.get(k);
                    

                    String _spriteSpriteName = object.sprite.name;
                    if (_spriteSpriteName == null)
                        _spriteSpriteName = "";
                    String _objectSpriteName = object.getSpriteName();
                    if (_objectSpriteName == null)
                        _objectSpriteName = "";
                    if (object.getSprite().getID() == this.spriteID
                            && _spriteSpriteName.equals(this.spriteSpriteName)
                            && _objectSpriteName.equals(this.objectSpriteName)) 
                    {
                        if (zombie.network.GameClient.connection != null)
                            sendPacketDestroyObjectonSquare(square, object);
                        square.RemoveTileObject(object);
                    }

                    

                }
            }
        }
    }

    

    /**
     * Called Event Handling Method
     *
     * @param key {@link NanoContext} in which NanoVG is initialized
     */
    @Override
    public void handleEvent(Integer key) {
        if (key == this.hotkey) {
            player = IsoPlayer.getInstance();
            gsquare = player.getSquare();
            x = gsquare.getX();
            y = gsquare.getY();
            cell = player.getCell();
            
            if (this.deleteTiles) deleteTiles();
            int k = 0;
            readDataFiles(player);
            if (this.restoreFloors) restoreFloor();
            if (this.restoreOtherObjects) restoreObjects();
            IsoLot.put(lot);
        }
    }

}

///// example of cheat tile creation algo, probably some code is missing. never used;

/*          
            IsoObject obj = IsoObject.getNew();
            obj.setType(IsoObjectType.MAX);
            obj.setSprite("carpentry_01_16");
            obj.sprite.setName("carpentry_01_16");
            obj.spriteName = "carpentry_01_16";   //this or above, makes same object looking like brazil one;
                    
            
            //obj.setSquare(gsquare);
            //gsquare.AddSpecialObject(obj); 
*/

