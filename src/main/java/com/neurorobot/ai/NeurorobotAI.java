package com.neurorobot.ai;

import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class NeurorobotAI {
    private final MobEntity entity;
    private String currentTask = "Idle";
    private PlayerEntity followTarget;
    private int followCooldown = 0;

    public NeurorobotAI(MobEntity entity) {
        this.entity = entity;
    }

    public boolean followPlayer(PlayerEntity player) {
        try {
            this.followTarget = player;
            currentTask = "Following " + player.getName().getString();

            // 立即开始跟随
            recalculateFollowPath();

            return true;
        } catch (Exception e) {
            System.err.println("Failed to start follow process: " + e.getMessage());
            return false;
        }
    }

    public boolean stopAllTasks() {
        try {
            this.followTarget = null;
            entity.getNavigation().stop();
            currentTask = "Idle";
            return true;
        } catch (Exception e) {
            System.err.println("Failed to stop tasks: " + e.getMessage());
            return false;
        }
    }

    public void tick() {
        if (followTarget != null) {
            followCooldown++;

            // 每10 tick（0.5秒）检查一次是否需要重新计算路径
            if (followCooldown >= 10) {
                recalculateFollowPath();
                followCooldown = 0;
            }

            // 确保看向玩家
            entity.getLookControl().lookAt(followTarget, 30.0F, 30.0F);
        }
    }

    private void recalculateFollowPath() {
        if (followTarget != null && !followTarget.isRemoved()) {
            double distance = entity.squaredDistanceTo(followTarget);

            // 如果距离超过2格，重新计算路径
            if (distance > 4.0) {
                boolean success = entity.getNavigation().startMovingTo(followTarget, 1.2);

                if (!success) {
                    // 如果路径查找失败，尝试简单的移动
                    double dx = followTarget.getX() - entity.getX();
                    double dz = followTarget.getZ() - entity.getZ();
                    double distanceHorizontal = Math.sqrt(dx * dx + dz * dz);

                    if (distanceHorizontal > 0.1) {
                        // 直接朝玩家方向移动一小段距离
                        entity.setVelocity(
                                dx / distanceHorizontal * 0.1,
                                entity.getVelocity().y,
                                dz / distanceHorizontal * 0.1
                        );
                    }
                }
            }
        }
    }

    public boolean mineResources() {
        // 停止跟随以进行挖掘
        this.followTarget = null;
        currentTask = "Mining (simulated)";
        return true;
    }

    public boolean exploreArea(int radius) {
        // 停止跟随以进行探索
        this.followTarget = null;

        try {
            // 随机移动探索
            BlockPos currentPos = entity.getBlockPos();
            BlockPos targetPos = currentPos.add(
                    entity.getRandom().nextInt(radius * 2) - radius,
                    0,
                    entity.getRandom().nextInt(radius * 2) - radius
            );

            boolean success = entity.getNavigation().startMovingTo(targetPos.getX(), targetPos.getY(), targetPos.getZ(), 1.0);

            if (success) {
                currentTask = "Exploring";
            }

            return success;
        } catch (Exception e) {
            System.err.println("Failed to start exploration: " + e.getMessage());
            return false;
        }
    }

    public boolean gotoPosition(BlockPos position) {
        // 停止跟随以移动到指定位置
        this.followTarget = null;

        try {
            boolean success = entity.getNavigation().startMovingTo(position.getX(), position.getY(), position.getZ(), 1.0);

            if (success) {
                currentTask = "Moving to position";
            }

            return success;
        } catch (Exception e) {
            System.err.println("Failed to go to position: " + e.getMessage());
            return false;
        }
    }

    public boolean isPathing() {
        return entity.getNavigation().isFollowingPath() || followTarget != null;
    }

    public boolean isFollowing() {
        return followTarget != null;
    }

    public String getCurrentGoal() {
        if (followTarget != null) {
            return "Following " + followTarget.getName().getString();
        }
        return currentTask;
    }
}