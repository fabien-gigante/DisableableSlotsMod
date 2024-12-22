package com.fabien_gigante;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import net.fabricmc.api.ModInitializer;

public class DisableableSlotsMod implements ModInitializer {
	public static final Logger LOGGER = LoggerFactory.getLogger("Disableable-slots");
	
	// Server-side mod entry point
	@Override
	public void onInitialize() {
		LOGGER.info("Disableable Slots - Mod starting...");
	}
}