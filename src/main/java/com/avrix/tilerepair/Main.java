package com.avrix.tilerepair;

import com.avrix.events.EventManager;
import com.avrix.plugin.Metadata;
import com.avrix.plugin.Plugin;
/**
 * Main entry point of the example plugin
 */
public class Main extends Plugin {
    /**
     * Constructs a new {@link Plugin} with the specified metadata.
     * Metadata is transferred when the plugin is loaded into the game context.
     *
     * @param metadata The {@link Metadata} associated with this plugin.
     */
    public Main(Metadata metadata) {
        super(metadata);
    }

    /**
     * Called when the plugin is initialized.
     * <p>
     * Implementing classes should override this method to provide the initialization logic.
     */
    @Override
    public void onInitialize() {
        loadDefaultConfig();

        int rad = Integer.parseInt(getDefaultConfig().getString("radius"));
        int key = Integer.parseInt(getDefaultConfig().getString("key"));
        int spriteID = Integer.parseInt(getDefaultConfig().getString("spriteID"));
        boolean restoreFloors = Boolean.parseBoolean(getDefaultConfig().getString("restoreFloors"));
        boolean restoreOtherObjects = Boolean.parseBoolean(getDefaultConfig().getString("restoreOtherObjects"));
        boolean deleteTiles = Boolean.parseBoolean(getDefaultConfig().getString("deleteTiles"));
        String floorName = getDefaultConfig().getString("floorname");
        String objectSpriteName = getDefaultConfig().getString("objectSpriteName");
        String spriteSpriteName = getDefaultConfig().getString("spriteSpriteName");;
        EventManager.addListener(new KeyHandler(15, 211, floorName, restoreFloors, restoreOtherObjects, deleteTiles, spriteSpriteName, objectSpriteName, spriteID));

        System.out.println("[#] Config: ");
        
    }
}