package com.tilerepair;
import java.awt.event.KeyEvent;
import java.util.LinkedHashMap;
import java.util.List;

import javax.swing.text.Keymap;

import com.avrix.enums.KeyEventType;
import com.avrix.events.EventManager;
import com.avrix.plugin.Metadata;
import com.avrix.plugin.Plugin;
import com.avrix.utils.YamlFile;
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
        YamlFile config = getDefaultConfig();

        List<Object>[] objectLists = new List[2];
        objectLists[1] = config.getList("tilesToRemoveList");
        objectLists[0] = config.getList("floorList");

        boolean[] flags = new boolean[3];
        flags[0] = config.getBoolean("deleteTiles");
        flags[1] = config.getBoolean("restoreFloors");
        flags[2] = config.getBoolean("restoreOtherObjects");

        String[] levels = new String[3];

        levels[0] = config.getString("levelsD");
        levels[1] = config.getString("levelsF");
        levels[2] = config.getString("levelsR");

        int rad = config.getInt("radius");
        int key = config.getInt("hotkey");

        int[] keys = new int[3];
        keys[0] = config.getInt("Dkey");
        keys[1] = config.getInt("Fkey");
        keys[2] = config.getInt("Rkey");

        EventManager.addListener(new KeyHandler(rad, key, flags, objectLists, levels, keys));

        System.out.println("[#] Config: ");
        
    }
}