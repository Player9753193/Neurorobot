package com.neurorobot;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.SpawnEggItem;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.neurorobot.entity.NeurorobotEntity;
import com.neurorobot.listener.NeurorobotChatListener;

public class Neurorobot implements ModInitializer {
    public static final String MOD_ID = "neurorobot";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    // 注册物品
    public static final Item BATTERY = Registry.register(Registries.ITEM,
            Identifier.of(MOD_ID, "battery"),
            new Item(new Item.Settings().maxCount(16)));

    public static final Item MECHANICAL_PART = Registry.register(Registries.ITEM,
            Identifier.of(MOD_ID, "mechanical_part"),
            new Item(new Item.Settings().maxCount(64)));

    // 注册神经机器人实体
    public static final EntityType<NeurorobotEntity> NEUROROBOT = Registry.register(
            Registries.ENTITY_TYPE,
            Identifier.of(MOD_ID, "neurorobot"),
            FabricEntityTypeBuilder.create(SpawnGroup.CREATURE, NeurorobotEntity::new)
                    .dimensions(EntityDimensions.fixed(0.6f, 1.8f))
                    .build()
    );

    // 神经机器人刷怪蛋
    public static final Item NEUROROBOT_SPAWN_EGG = Registry.register(Registries.ITEM,
            Identifier.of(MOD_ID, "neurorobot_spawn_egg"),
            new SpawnEggItem(NEUROROBOT, 0x4A4A4A, 0x00FF00, new Item.Settings().maxCount(4))
    );

    // 创建创意模式物品栏
    private static final ItemGroup NEUROROBOT_GROUP = FabricItemGroup.builder()
            .icon(() -> new ItemStack(BATTERY))
            .displayName(Text.translatable("itemGroup.neurorobot.main"))
            .entries((context, entries) -> {
                entries.add(BATTERY);
                entries.add(MECHANICAL_PART);
                entries.add(NEUROROBOT_SPAWN_EGG);
            })
            .build();

    @Override
    public void onInitialize() {
        // 注册创意模式物品栏
        Registry.register(Registries.ITEM_GROUP, Identifier.of(MOD_ID, "main"), NEUROROBOT_GROUP);

        // 注册实体属性
        FabricDefaultAttributeRegistry.register(NEUROROBOT, NeurorobotEntity.createNeurorobotAttributes());

        // 注册聊天监听器
        NeurorobotChatListener.register();

        LOGGER.info("Neurorobot mod initialized with AI dialogue system!");
    }
}