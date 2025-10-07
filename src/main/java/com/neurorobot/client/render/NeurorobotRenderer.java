// 创建新的简化版 NeurorobotRenderer.java
package com.neurorobot.client.render;

import com.neurorobot.entity.NeurorobotEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.MobEntityRenderer;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.RotationAxis;

public class NeurorobotRenderer extends MobEntityRenderer<NeurorobotEntity, PlayerEntityModel<NeurorobotEntity>> {
    private static final Identifier TEXTURE = Identifier.of("neurorobot", "textures/entity/neurorobot.png");

    public NeurorobotRenderer(EntityRendererFactory.Context context) {
        super(context, new PlayerEntityModel<>(context.getPart(EntityModelLayers.PLAYER), false), 0.5f);
    }

    @Override
    public Identifier getTexture(NeurorobotEntity entity) {
        return TEXTURE;
    }

    @Override
    public void render(NeurorobotEntity entity, float f, float g, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i) {
        super.render(entity, f, g, matrixStack, vertexConsumerProvider, i);
        renderHeldItem(entity, matrixStack, vertexConsumerProvider, i);
    }

    private void renderHeldItem(NeurorobotEntity entity, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int light) {
        ItemStack heldItem = entity.getMainHandStack();
        if (heldItem.isEmpty()) {
            return;
        }

        matrixStack.push();

        // 调整到合适的位置（右手位置）
        matrixStack.translate(0.0, 1.5, 0.0); // 调整到模型中心
        matrixStack.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(180.0F)); // 旋转模型

        // 右手位置
        matrixStack.translate(0.4, -0.8, -0.1);

        // 调整物品旋转
        matrixStack.multiply(RotationAxis.POSITIVE_X.rotationDegrees(90.0F));
        matrixStack.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(180.0F));

        // 渲染物品
        MinecraftClient.getInstance().getItemRenderer().renderItem(
                heldItem,
                ModelTransformationMode.THIRD_PERSON_RIGHT_HAND,
                light,
                net.minecraft.client.render.OverlayTexture.DEFAULT_UV,
                matrixStack,
                vertexConsumerProvider,
                entity.getWorld(),
                entity.getId()
        );

        matrixStack.pop();
    }
}
