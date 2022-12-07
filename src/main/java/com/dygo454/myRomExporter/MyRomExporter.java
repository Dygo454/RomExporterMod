package com.dygo454.myRomExporter;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;

import java.util.logging.*;

public class MyRomExporter implements ModInitializer {
    public static final String MOD_ID = "myromexp";
    public static final Logger LOGGER = LogManager.getLogManager().getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> ExportRomCommand.register(dispatcher));
    }
}
