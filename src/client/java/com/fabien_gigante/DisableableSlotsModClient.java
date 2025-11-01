package com.fabien_gigante;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import net.fabricmc.api.ClientModInitializer;

public class DisableableSlotsModClient implements ClientModInitializer {
	public static final Logger LOGGER = LoggerFactory.getLogger("Disableable Slots (client)");
	
	// Client-side mod entry point
	@Override
	public void onInitializeClient() {
		LOGGER.info("Disableable Slots - Mod starting (client)...");
	}
}