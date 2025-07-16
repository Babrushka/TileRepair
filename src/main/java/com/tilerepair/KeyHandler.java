package com.tilerepair;
import java.util.List;

import com.avrix.events.OnKeyPressedEvent;
import com.avrix.ui.NanoContext;




/**
 * Draw HUD
 */
public class KeyHandler extends OnKeyPressedEvent {
    int hotkey;
    int[] keys;
    boolean restoreFloors, deleteTiles, restoreOtherObjects;

    private TileRepair CTileRepair = null;
    public KeyHandler(int _radius, int _key, boolean[] _flags, List<Object>[] _TilesLists, String[] _levels, int[] _keys) {

        this.hotkey = _key;

        this.deleteTiles = _flags[0];
        this.restoreFloors = _flags[1];
        this.restoreOtherObjects = _flags[2];

        this.keys = _keys;
        this.CTileRepair = new TileRepair(_radius, _TilesLists, _levels);
    }

    

    /**
     * Called Event Handling Method
     *
     * @param key {@link NanoContext} in which NanoVG is initialized
     */
    @Override
    public void handleEvent(Integer key) {
        if (key == this.hotkey) {

            CTileRepair.Init();
            if (this.deleteTiles)
                CTileRepair.deleteTiles();

            if (this.restoreFloors || this.restoreOtherObjects) {

                CTileRepair.readDataFiles();

                if (this.restoreFloors) {
                    CTileRepair.restoreFloors();
                }
                if (this.restoreOtherObjects) {
                    CTileRepair.restoreObjects();
                }

                CTileRepair.clearLot();
            }

        }
        else if (key == keys[0]) {
            CTileRepair.Init();
            if (this.deleteTiles)
                CTileRepair.deleteTiles();
        }
        else if (key == keys[1]) {
            CTileRepair.Init();
            if (this.restoreFloors || this.restoreOtherObjects) {

                CTileRepair.readDataFiles();

                if (this.restoreFloors) {
                    CTileRepair.restoreFloors();
                }
                if (this.restoreOtherObjects) {
                    //CTileRepair.restoreObjects();
                }

                CTileRepair.clearLot();
            }
        }
        else if (key == keys[2]) {
            if (this.restoreFloors || this.restoreOtherObjects) {

                CTileRepair.readDataFiles();

                if (this.restoreFloors) {
                    //CTileRepair.restoreFloors();
                }
                if (this.restoreOtherObjects) {
                    CTileRepair.restoreObjects();
                }

                CTileRepair.clearLot();
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

