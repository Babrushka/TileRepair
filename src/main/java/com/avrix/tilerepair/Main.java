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
        EventManager.addListener(new KeyHandler(15,211));

        System.out.println("[#] Config: ");
        
    }
}