package com.neurorobot;

import com.neurorobot.entity.NeurorobotEntities;
import com.neurorobot.event.ChatListener;
import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Neurorobot implements ModInitializer {
    public static final String MOD_ID = "neurorobot";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        NeurorobotEntities.registerEntities();
        ChatListener.register(); // 注册聊天监听器
        LOGGER.info("Neurorobot mod initialized!");
    }
}
