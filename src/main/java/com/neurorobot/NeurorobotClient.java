package com.neurorobot;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.client.render.entity.model.EntityModelLayer;
import net.minecraft.util.Identifier;
import com.neurorobot.client.model.NeurorobotModel;
import com.neurorobot.client.render.NeurorobotEntityRenderer;

public class NeurorobotClient implements ClientModInitializer {
    public static final EntityModelLayer NEUROROBOT_MODEL_LAYER = new EntityModelLayer(Identifier.of("neurorobot", "neurorobot"), "main");

    @Override
    public void onInitializeClient() {
        // 注册模型层
        EntityModelLayerRegistry.registerModelLayer(NEUROROBOT_MODEL_LAYER, NeurorobotModel::getTexturedModelData);

        // 注册实体渲染器
        EntityRendererRegistry.register(Neurorobot.NEUROROBOT, NeurorobotEntityRenderer::new);
    }
}