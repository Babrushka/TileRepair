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
    public KeyHandler(int _radius, List<Object>[] _TilesLists, String _levels, int[] _keys) {

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
        if (key == this.keys[0] || key == this.keys[1] || key == this.keys[2] || key == this.keys[3]) {
            CTileRepair.Init();

            if (key == this.keys[0]) {
                CTileRepair.deleteTiles();
            } else if (key == this.keys[3]){
                CTileRepair.lockContainers();

            } else if (key == this.keys[1] || key == this.keys[2]) {

                CTileRepair.readDataFiles();
                if (key == this.keys[1] ) {
                    CTileRepair.restoreFloors();
                } else if (key == this.keys[2]) {
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

