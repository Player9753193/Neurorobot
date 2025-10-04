package com.neurorobot.baritone;

import net.minecraft.entity.mob.MobEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.List;

public class BaritoneController {
    private final MobEntity entity;
    private boolean isActive = false;
    private String currentTask = "None";

    public BaritoneController(MobEntity entity) {
        this.entity = entity;
        // 简化初始化，稍后根据实际 Baritone API 调整
        this.isActive = true;
    }

    public boolean followPlayer(net.minecraft.entity.player.PlayerEntity player) {
        if (!isActive) return false;

        try {
            // 简化跟随逻辑 - 使用原版路径查找
            entity.getNavigation().startMovingTo(player, 1.0);
            currentTask = "Following " + player.getName().getString();
            return true;
        } catch (Exception e) {
            System.err.println("Failed to start follow process: " + e.getMessage());
            return false;
        }
    }

    public boolean stopAllTasks() {
        if (!isActive) return false;

        try {
            entity.getNavigation().stop();
            currentTask = "Idle";
            return true;
        } catch (Exception e) {
            System.err.println("Failed to stop tasks: " + e.getMessage());
            return false;
        }
    }

    public boolean mineBlocks(String... blockNames) {
        if (!isActive) return false;

        try {
            // 简化挖掘逻辑 - 在实际集成中，这里会调用 Baritone 的挖掘过程
            List<Block> targetBlocks = new ArrayList<>();

            for (String blockName : blockNames) {
                Block block = parseBlockName(blockName);
                if (block != null) {
                    targetBlocks.add(block);
                }
            }

            if (targetBlocks.isEmpty()) {
                // 默认挖掘常见矿石
                targetBlocks.add(Blocks.COAL_ORE);
                targetBlocks.add(Blocks.IRON_ORE);
                targetBlocks.add(Blocks.GOLD_ORE);
                targetBlocks.add(Blocks.DIAMOND_ORE);
            }

            currentTask = "Mining " + targetBlocks.get(0).getName().getString();

            // 在实际集成中，这里会调用 Baritone 的挖掘功能
            // baritone.getMineProcess().mine(targetBlocks.toArray(new Block[0]));

            // 简化：发送消息表示开始挖掘
//            if (entity instanceof com.neurorobot.entity.NeurorobotEntity neurorobot) {
//                neurorobot.sendSimulatedMiningMessage(targetBlocks.get(0));
//            }

            return true;
        } catch (Exception e) {
            System.err.println("Failed to start mining process: " + e.getMessage());
            return false;
        }
    }

    public boolean mineSpecificBlock(String blockName) {
        Block block = parseBlockName(blockName);
        if (block != null) {
            return mineBlocks(blockName);
        }
        return false;
    }

    private Block parseBlockName(String blockName) {
        try {
            // 处理常见的方块名称映射
            switch (blockName.toLowerCase()) {
                case "gold":
                case "gold_ore":
                    return Blocks.GOLD_ORE;
                case "iron":
                case "iron_ore":
                    return Blocks.IRON_ORE;
                case "diamond":
                case "diamond_ore":
                    return Blocks.DIAMOND_ORE;
                case "coal":
                case "coal_ore":
                    return Blocks.COAL_ORE;
                case "redstone":
                case "redstone_ore":
                    return Blocks.REDSTONE_ORE;
                case "lapis":
                case "lapis_ore":
                    return Blocks.LAPIS_ORE;
                case "emerald":
                case "emerald_ore":
                    return Blocks.EMERALD_ORE;
                case "copper":
                case "copper_ore":
                    return Blocks.COPPER_ORE;
                case "stone":
                    return Blocks.STONE;
                case "cobblestone":
                    return Blocks.COBBLESTONE;
                case "dirt":
                    return Blocks.DIRT;
                case "gravel":
                    return Blocks.GRAVEL;
                case "sand":
                    return Blocks.SAND;
                default:
                    // 尝试通过注册表查找
                    Identifier blockId = Identifier.tryParse(blockName);
                    if (blockId != null) {
                        return Registries.BLOCK.get(blockId);
                    }
                    return null;
            }
        } catch (Exception e) {
            return null;
        }
    }

    public boolean exploreArea(int radius) {
        if (!isActive) return false;

        try {
            // 简化探索逻辑
            currentTask = "Exploring (radius: " + radius + ")";

            // 在实际集成中，这里会调用 Baritone 的探索功能
            // baritone.getExploreProcess().explore(center.getX(), center.getZ());

            return true;
        } catch (Exception e) {
            System.err.println("Failed to start exploration: " + e.getMessage());
            return false;
        }
    }

    public boolean gotoPosition(BlockPos position) {
        if (!isActive) return false;

        try {
            // 简化位置移动逻辑
            entity.getNavigation().startMovingTo(position.getX(), position.getY(), position.getZ(), 1.0);
            currentTask = "Moving to position";
            return true;
        } catch (Exception e) {
            System.err.println("Failed to go to position: " + e.getMessage());
            return false;
        }
    }

    public boolean isActive() {
        return isActive;
    }

    public boolean isPathing() {
        return entity.getNavigation().isFollowingPath();
    }

    public String getCurrentGoal() {
        return currentTask;
    }
}