package com.neurorobot.baritone;

import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;

import java.util.ArrayList;
import java.util.List;

public class SimpleBaritoneIntegration {
    private final MobEntity entity;
    private boolean isActive = true; // 总是激活，使用原版路径查找

    public SimpleBaritoneIntegration(MobEntity entity) {
        this.entity = entity;
    }

    public boolean followPlayer(PlayerEntity player) {
        if (!isActive) return false;

        try {
            // 使用原版路径查找系统
            boolean success = entity.getNavigation().startMovingTo(player, 1.0);

            // 如果路径查找失败，尝试直接看向玩家
            if (!success) {
                entity.getLookControl().lookAt(player, 30.0F, 30.0F);
            }

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
            return true;
        } catch (Exception e) {
            System.err.println("Failed to stop tasks: " + e.getMessage());
            return false;
        }
    }

    public boolean mineBlocks(String... blockNames) {
        if (!isActive) return false;

        try {
            // 简化版本：移动到最近的指定方块
            // 在实际实现中，这里会搜索世界中的目标方块
            return true;
        } catch (Exception e) {
            System.err.println("Failed to start mining process: " + e.getMessage());
            return false;
        }
    }

    public boolean mineSpecificBlock(String blockName) {
        return mineBlocks(blockName);
    }

    public boolean exploreArea(int radius) {
        if (!isActive) return false;

        try {
            // 简化版本：随机移动
            entity.getNavigation().startMovingTo(
                    entity.getX() + (entity.getRandom().nextDouble() - 0.5) * radius,
                    entity.getY(),
                    entity.getZ() + (entity.getRandom().nextDouble() - 0.5) * radius,
                    1.0
            );
            return true;
        } catch (Exception e) {
            System.err.println("Failed to start exploration: " + e.getMessage());
            return false;
        }
    }

    public boolean gotoPosition(BlockPos position) {
        if (!isActive) return false;

        try {
            boolean success = entity.getNavigation().startMovingTo(position.getX(), position.getY(), position.getZ(), 1.0);
            return success;
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
        if (!isActive) return "Inactive";

        if (entity.getNavigation().isFollowingPath()) {
            return "Pathing to target";
        } else {
            return "Idle";
        }
    }
}