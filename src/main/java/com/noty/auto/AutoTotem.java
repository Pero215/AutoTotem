package com.noty.auto;

import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The main entry point for the AutoTotem mod.
 * Since this is a client-only utility, most logic resides in the client entrypoint.
 */
public class AutoTotem implements ModInitializer {
    public static final String MOD_ID = "autototem";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        LOGGER.info("AutoTotem Mod Initialized (Client-side logic loads separately).");
    }
}