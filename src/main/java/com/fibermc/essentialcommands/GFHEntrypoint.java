package com.fibermc.essentialcommands;

import net.devtech.grossfabrichacks.entrypoints.PrePreLaunch;
import net.devtech.grossfabrichacks.instrumentation.InstrumentationApi;
import org.apache.logging.log4j.LogManager;

public class GFHEntrypoint implements PrePreLaunch {

    @Override
    public void onPrePreLaunch() {
        InstrumentationApi.pipeClassThroughTransformerBootstrap("com/mojang/brigadier/tree/CommandNode");
        LogManager.getLogger("ECPreLaunch").info("Completed Essential Commands pre-pre-launch tasks!");
    }
}
