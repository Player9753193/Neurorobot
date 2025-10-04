package com.neurorobot.client.render;

import com.neurorobot.NeurorobotClient;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.MobEntityRenderer;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import com.neurorobot.Neurorobot;
import com.neurorobot.client.model.NeurorobotModel;
import com.neurorobot.entity.NeurorobotEntity;

public class NeurorobotEntityRenderer extends MobEntityRenderer<NeurorobotEntity, NeurorobotModel> {
    private static final Identifier TEXTURE = Identifier.of("neurorobot", "textures/entity/neurorobot.png");

    public NeurorobotEntityRenderer(EntityRendererFactory.Context context) {
        super(context, new NeurorobotModel(context.getPart(NeurorobotClient.NEUROROBOT_MODEL_LAYER)), 0.5f);
    }

    @Override
    public Identifier getTexture(NeurorobotEntity entity) {
        return TEXTURE;
    }

    @Override
    public void render(NeurorobotEntity mobEntity, float f, float g, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i) {
        // 可以在这里添加自定义渲染逻辑
        super.render(mobEntity, f, g, matrixStack, vertexConsumerProvider, i);
    }

    @Override
    protected void scale(NeurorobotEntity entity, MatrixStack matrices, float amount) {
        // 调整实体大小
        matrices.scale(0.8f, 0.8f, 0.8f);
    }
}