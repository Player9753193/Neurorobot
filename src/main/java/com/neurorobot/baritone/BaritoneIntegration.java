package com.neurorobot.baritone;

import baritone.api.BaritoneAPI;
import baritone.api.IBaritone;
import baritone.api.process.IGetToBlockProcess;
import baritone.api.process.IMineProcess;
import baritone.api.process.IExploreProcess;
import baritone.api.pathing.goals.Goal;
import baritone.api.pathing.goals.GoalBlock;
import baritone.api.pathing.goals.GoalNear;
import baritone.api.utils.BetterBlockPos;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.List;

public class BaritoneIntegration {
    private final MobEntity entity;
    private IBaritone baritone;
    private boolean isActive = false;

    public BaritoneIntegration(MobEntity entity) {
        this.entity = entity;
        initializeBaritone();
    }

    private void initializeBaritone() {
        try {
            // 由于 Baritone 主要是为玩家设计的，我们需要创建一个虚拟的 Baritone 实例
            // 或者使用现有的 Baritone 实例
            this.baritone = BaritoneAPI.getProvider().getPrimaryBaritone();
            this.isActive = true;
        } catch (Exception e) {
            System.err.println("Failed to initialize Baritone for Neurorobot: " + e.getMessage());
            this.isActive = false;
        }
    }

    public boolean followPlayer(PlayerEntity player) {
        if (!isActive || baritone == null) return false;

        try {
            // 创建一个目标位置在玩家附近
            Goal goal = new GoalNear(new BetterBlockPos(player.getBlockPos()), 2);
            baritone.getCustomGoalProcess().setGoal(goal);

            return true;
        } catch (Exception e) {
            System.err.println("Failed to start follow process: " + e.getMessage());
            return false;
        }
    }

    public boolean stopAllTasks() {
        if (!isActive || baritone == null) return false;

        try {
            baritone.getPathingBehavior().cancelEverything();
            return true;
        } catch (Exception e) {
            System.err.println("Failed to stop tasks: " + e.getMessage());
            return false;
        }
    }

    public boolean mineBlocks(String... blockNames) {
        if (!isActive || baritone == null) return false;

        try {
            IMineProcess mineProcess = baritone.getMineProcess();

            // 将方块名称转换为 Baritone 可识别的格式
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

            mineProcess.mine(targetBlocks.toArray(new Block[0]));
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
        if (!isActive || baritone == null) return false;

        try {
            IExploreProcess exploreProcess = baritone.getExploreProcess();
            BlockPos center = entity.getBlockPos();
            exploreProcess.explore(center.getX(), center.getZ());
            return true;
        } catch (Exception e) {
            System.err.println("Failed to start exploration: " + e.getMessage());
            return false;
        }
    }

    public boolean gotoPosition(BlockPos position) {
        if (!isActive || baritone == null) return false;

        try {
            IGetToBlockProcess gotoProcess = baritone.getGetToBlockProcess();
            Goal goal = new GoalBlock(new BetterBlockPos(position));
            baritone.getCustomGoalProcess().setGoal(goal);
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
        return isActive && baritone != null && baritone.getPathingBehavior().isPathing();
    }

    public String getCurrentGoal() {
        if (!isActive || baritone == null) return "Inactive";

        try {
            if (baritone.getMineProcess().isActive()) {
                return "Mining";
            } else if (baritone.getFollowProcess().isActive()) {
                return "Following";
            } else if (baritone.getExploreProcess().isActive()) {
                return "Exploring";
            } else if (baritone.getGetToBlockProcess().isActive()) {
                return "Going to position";
            } else if (baritone.getPathingBehavior().isPathing()) {
                return "Pathing";
            } else {
                return "Idle";
            }
        } catch (Exception e) {
            return "Unknown";
        }
    }
}