package com.neurorobot.client.model;

import com.neurorobot.entity.NeurorobotEntity;
import net.minecraft.client.model.*;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;

public class NeurorobotModel extends EntityModel<NeurorobotEntity> {
    private final ModelPart Head;
    private final ModelPart Body;
    private final ModelPart LeftArm;
    private final ModelPart RightArm;
    private final ModelPart LeftLeg;
    private final ModelPart RightLeg;
    private final ModelPart root;

    public NeurorobotModel(ModelPart root) {
        this.root = root;
        this.Head = root.getChild("Head");
        this.Body = root.getChild("Body");
        this.LeftArm = root.getChild("LeftArm");
        this.RightArm = root.getChild("RightArm");
        this.LeftLeg = root.getChild("LeftLeg");
        this.RightLeg = root.getChild("RightLeg");
    }

    public static TexturedModelData getTexturedModelData() {
        ModelData modelData = new ModelData();
        ModelPartData modelPartData = modelData.getRoot();
        ModelPartData Head = modelPartData.addChild("Head", ModelPartBuilder.create().uv(0, 18).cuboid(-4.0F, -21.0F, -4.0F, 8.0F, 8.0F, 8.0F, new Dilation(0.0F)).uv(0, 0).cuboid(-4.5F, -21.5F, -4.5F, 9.0F, 9.0F, 9.0F, new Dilation(0.0F)), ModelTransform.pivot(0.0F, 13.0F, 0.0F));

        ModelPartData Body = modelPartData.addChild("Body", ModelPartBuilder.create().uv(32, 18).cuboid(-4.0F, -13.0F, -2.0F, 8.0F, 12.0F, 4.0F, new Dilation(0.0F)), ModelTransform.pivot(0.0F, 13.0F, 0.0F));

        ModelPartData LeftArm = modelPartData.addChild("LeftArm", ModelPartBuilder.create().uv(0, 34).cuboid(-2.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, new Dilation(0.0F)), ModelTransform.pivot(6.0F, 2.0F, 0.0F));

        ModelPartData RightArm = modelPartData.addChild("RightArm", ModelPartBuilder.create().uv(16, 34).cuboid(-2.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, new Dilation(0.0F)), ModelTransform.pivot(-6.0F, 2.0F, 0.0F));

        ModelPartData LeftLeg = modelPartData.addChild("LeftLeg", ModelPartBuilder.create().uv(32, 34).cuboid(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, new Dilation(0.0F)), ModelTransform.pivot(2.0F, 12.0F, 0.0F));

        ModelPartData RightLeg = modelPartData.addChild("RightLeg", ModelPartBuilder.create().uv(36, 0).cuboid(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, new Dilation(0.0F)), ModelTransform.pivot(-2.0F, 12.0F, 0.0F));
        return TexturedModelData.of(modelData, 64, 64);
    }

    @Override
    public void setAngles(NeurorobotEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        // 设置头部旋转
        this.Head.yaw = netHeadYaw * (float)(Math.PI / 180.0);
        this.Head.pitch = headPitch * (float)(Math.PI / 180.0);

        // 设置四肢动画
        this.RightArm.pitch = (float) (Math.cos(limbSwing * 0.6662F + Math.PI) * 2.0F * limbSwingAmount * 0.5F);
        this.LeftArm.pitch = (float) (Math.cos(limbSwing * 0.6662F) * 2.0F * limbSwingAmount * 0.5F);
        this.RightArm.roll = 0.0F;
        this.LeftArm.roll = 0.0F;
        this.RightLeg.pitch = (float) (Math.cos(limbSwing * 0.6662F) * 1.4F * limbSwingAmount);
        this.LeftLeg.pitch = (float) (Math.cos(limbSwing * 0.6662F + Math.PI) * 1.4F * limbSwingAmount);
        this.RightLeg.yaw = 0.0F;
        this.LeftLeg.yaw = 0.0F;
    }

    @Override
    public void render(MatrixStack matrices, VertexConsumer vertexConsumer, int light, int overlay, int color) {
        // 在1.21中，render方法现在接受一个int颜色参数而不是分开的rgba
        float red = ((color >> 16) & 0xFF) / 255.0f;
        float green = ((color >> 8) & 0xFF) / 255.0f;
        float blue = (color & 0xFF) / 255.0f;
        float alpha = ((color >> 24) & 0xFF) / 255.0f;

        Head.render(matrices, vertexConsumer, light, overlay, color);
        Body.render(matrices, vertexConsumer, light, overlay, color);
        LeftArm.render(matrices, vertexConsumer, light, overlay, color);
        RightArm.render(matrices, vertexConsumer, light, overlay, color);
        LeftLeg.render(matrices, vertexConsumer, light, overlay, color);
        RightLeg.render(matrices, vertexConsumer, light, overlay, color);
    }
}