package com.example.crimetown;

import com.example.crimetown.network.ModNetworking;
import com.example.crimetown.registry.ModComponents;
import com.example.crimetown.registry.ModItems;
import com.example.crimetown.registry.ModSounds;
import net.fabricmc.api.ModInitializer;

/** Common entrypoint: runs on the client, the integrated server and the dedicated server. */
public class CrimeTown implements ModInitializer {
    public static final String MOD_ID = "crimetown";

    @Override
    public void onInitialize() {
        ModSounds.init();
        ModComponents.init();
        ModItems.init();
        ModNetworking.init();
        TownEvents.init();
    }
}
