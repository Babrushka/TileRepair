package com.avrix.tilerepair;

import com.avrix.events.OnKeyPressedEvent;

import java.io.File;
import java.lang.reflect.Field;
import java.util.ArrayList;

import com.avrix.ui.NanoContext;

import gnu.trove.list.array.TIntArrayList;
import zombie.characters.IsoPlayer;
import zombie.core.network.ByteBufferWriter;

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
    private int x, y, wx, wy;
    private IsoSprite[][][] defaultSprites;
    private int[][] overlays;
    private IsoPlayer player;
    private IsoGridSquare gsquare;
    private IsoChunk chunk;
    private IsoCell cell;

    public KeyHandler(int radius, int key) {
        this.radius = radius;
        this.hotkey = key;
        this.defaultSprites = new IsoSprite[10][10][5];
        this.overlays = new int[10][10];
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

        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 10; j++) {
                overlays[i][j] = 0;
                for (int h = 0; h < 1; h++) {
                    int addr = m_offsetInData[i + j * 10 + h * 100];
                    if (addr != -1) {
                        int tilescount = m_data.getQuick(addr);
                        if (tilescount > 0) {
                            for (int spriteindex = 0; spriteindex < tilescount; spriteindex++) {
                                String string = tilesUsed.get(m_data.getQuick(addr + 1 + spriteindex));
                                IsoSprite sprite = IsoSpriteManager.instance.NamedMap.get(string);
                                if (sprite.solidfloor)
                                    defaultSprites[i][j][0] = sprite;
                                if (sprite.getProperties().Is(IsoFlagType.FloorOverlay)){
                                    overlays[i][j]++;
                                    defaultSprites[i][j][overlays[i][j]] = sprite;
                                }
                            }
                        }
                    }
                }
            }

        }

    }

    private void getDefaultMapSpritesAroundPlayer(IsoPlayer player) {
        chunk = player.getChunk();
        wx = chunk.wx;
        wy = chunk.wy;
        String datafilename = "world_" + wx / 30 + "_" + wy / 30 + ".lotpack";
        if (!IsoLot.InfoFileNames.containsKey(datafilename)) {
            DebugLog.log("LoadCellBinaryChunk: NO SUCH LOT " + datafilename);
        } else {
            File file = new File(IsoLot.InfoFileNames.get(datafilename));
            if (file.exists()) {
                IsoLot lot = null;
                lot = IsoLot.get(wx / 30, wy / 30, wx, wy, chunk);
                this.ReadDefaultSprites(lot);
                IsoLot.put(lot);
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
        if (key == hotkey) {
            int count = 0;
            player = IsoPlayer.getInstance();
            gsquare = player.getSquare();
            x = gsquare.getX();
            y = gsquare.getY();
            cell = player.getCell();
            this.getDefaultMapSpritesAroundPlayer(player);

            for (int i = (x - radius); i <= (x + radius); i++) {
                for (int j = y - radius; j <= (y + radius); j++) {
                    IsoGridSquare square = cell.getGridSquare(i, j, 0);
                    PZArrayList<IsoObject> objects = square.getObjects();
                    for (int k = objects.size() - 1; k >= 0; k--) {
                        IsoObject object = objects.get(k);
                        if (object.getSpriteName() != null) {

                            if (object.getSprite().getID() == 20000000
                                    && object.getSpriteName().equals("carpentry_01_16")) {
                                if (zombie.network.GameClient.connection != null)
                                    sendPacketDestroyObjectonSquare(square, object);
                                square.RemoveTileObject(object);
                                count++;
                            }

                        }

                    }
                }
            }
            for (int i = 0; i < 10; i++) {
                for (int j = 0; j < 10; j++) {
                    IsoGridSquare square = cell.getGridSquare(wx * 10 + i, wy * 10 + j, 0);
                    PZArrayList<IsoObject> objects = square.getObjects();
                    for (int k = objects.size() - 1; k >= 0; k--) {
                        IsoObject object = objects.get(k);
                        IsoSprite sprite1 = object.getSprite();
                        String str = sprite1.getName();
                        if (str == null) str = "null";
                        //if (sprite1.solidfloor && str.equals("carpentry_02_58")) {
                        if (sprite1.solidfloor) {
                            object.clearAttachedAnimSprite();
                            object.setSprite(defaultSprites[i][j][0]);
                            if (overlays[i][j] > 0) {
                                if (object.AttachedAnimSprite == null) {
                                    object.AttachedAnimSprite = new ArrayList<>(4);
                                }
                                for (int o=0;o<overlays[i][j];o++)
                                {
                                    object.AttachedAnimSprite.add(IsoSpriteInstance.get(defaultSprites[i][j][o+1]));
                                }
                                
                            }
                            
                            
                            
                            if (zombie.network.GameClient.connection != null)
                                object.transmitUpdatedSpriteToServer();
                                
                        }
                    }
                }
            }

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

