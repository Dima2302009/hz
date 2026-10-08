package com.example.dread;

import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DreadMod implements ModInitializer {
    public static final String MOD_ID = "dread";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        ModEntities.register();
        ModItems.register();
        HorrorDirector.init();
        LOGGER.info("Dread загружен. Не оглядывайся.");
    }
}
