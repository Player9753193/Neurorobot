package com.neurorobot.client;

import com.neurorobot.client.render.NeurorobotRenderer;
import com.neurorobot.entity.NeurorobotEntities;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;

public class NeurorobotClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        EntityRendererRegistry.register(NeurorobotEntities.NEUROROBOT, NeurorobotRenderer::new);
    }
}
