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

        String levels = config.getString("levels");

        int rad = config.getInt("radius");

        int[] keys = new int[4];
        keys[0] = config.getInt("Dkey");
        keys[1] = config.getInt("Fkey");
        keys[2] = config.getInt("Rkey");
        keys[3] = config.getInt("Ckey");

        EventManager.addListener(new KeyHandler(rad, objectLists, levels, keys));

        System.out.println("[#] Config: ");
        
    }
}