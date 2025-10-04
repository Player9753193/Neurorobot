package com.neurorobot.entity;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.ai.goal.*;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import com.neurorobot.Neurorobot;
import com.neurorobot.dialogue.NeurorobotDialogueManager;
import com.neurorobot.ai.NeurorobotAI;

import java.util.EnumSet;

public class NeurorobotEntity extends PathAwareEntity {
    private final NeurorobotDialogueManager dialogueManager;
    private final NeurorobotAI neurorobotAI;
    private PlayerEntity currentCommander;
    private AIState aiState = AIState.IDLE;
    private int statusReportTimer = 0;

    public NeurorobotEntity(EntityType<? extends PathAwareEntity> entityType, World world) {
        super(entityType, world);
        this.experiencePoints = 5;
        this.dialogueManager = new NeurorobotDialogueManager(this);
        this.neurorobotAI = new NeurorobotAI(this);

        // 确保导航系统正确初始化
        this.getNavigation().setSpeed(1.2);
    }

    @Override
    protected void initGoals() {
        // 基本行为目标
        this.goalSelector.add(0, new SwimGoal(this));
        this.goalSelector.add(1, new LookAtEntityGoal(this, PlayerEntity.class, 8.0f));
        this.goalSelector.add(2, new LookAroundGoal(this));
        this.goalSelector.add(3, new WanderAroundGoal(this, 0.8));

        this.targetSelector.add(1, new RevengeGoal(this));
    }

    public static DefaultAttributeContainer.Builder createNeurorobotAttributes() {
        return PathAwareEntity.createMobAttributes()
                .add(EntityAttributes.GENERIC_MAX_HEALTH, 50.0)
                .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.5) // 进一步提高移动速度
                .add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 6.0)
                .add(EntityAttributes.GENERIC_ARMOR, 4.0)
                .add(EntityAttributes.GENERIC_FOLLOW_RANGE, 64.0); // 增加跟随范围
    }

    @Override
    protected ActionResult interactMob(PlayerEntity player, Hand hand) {
        ItemStack itemStack = player.getStackInHand(hand);

        if (itemStack.getItem() == Neurorobot.BATTERY) {
            if (!player.getAbilities().creativeMode) {
                itemStack.decrement(1);
            }

            this.heal(15.0f);
            player.addExperience(5);
            this.playSound(this.getEatSound(itemStack), 1.0f, 1.0f);

            sendMessageToPlayer(player, "Thank you for the battery! Systems recharged!");

            return ActionResult.SUCCESS;
        }

        // 右键点击显示状态
        if (!this.getWorld().isClient()) {
            sendMessageToPlayer(player, getStatusReport());
        }

        return ActionResult.PASS;
    }

    @Override
    public void tick() {
        super.tick();

        // 更新 AI
        neurorobotAI.tick();

        // 更新 AI 状态
        updateAIState();

        // 定期报告状态
        statusReportTimer++;
        if (statusReportTimer >= 400 && currentCommander != null && !this.getWorld().isClient()) { // 每20秒报告一次
            sendMessageToPlayer(currentCommander, "Status: " + getStatusReport());
            statusReportTimer = 0;
        }
    }

    private void updateAIState() {
        if (neurorobotAI.isFollowing()) {
            this.aiState = AIState.FOLLOWING;
        } else if (neurorobotAI.isPathing()) {
            this.aiState = AIState.WORKING;
        } else {
            this.aiState = AIState.IDLE;
        }
    }

    // 处理玩家聊天消息
    public void processPlayerMessage(PlayerEntity player, String message) {
        this.currentCommander = player;
        dialogueManager.processMessage(player, message);
    }

    // 发送消息给玩家
    public void sendMessageToPlayer(PlayerEntity player, String message) {
        if (!this.getWorld().isClient()) {
            player.sendMessage(Text.literal("§6[Neurorobot] §f" + message), false);
        }
    }

    // 跟随玩家
    public boolean followPlayer(PlayerEntity player) {
        this.currentCommander = player;

        // 开始跟随
        boolean success = neurorobotAI.followPlayer(player);

        if (success) {
            setAiState(AIState.FOLLOWING);
            sendMessageToPlayer(player, "I'll follow you wherever you go!");
        } else {
            sendMessageToPlayer(player, "I'm having trouble starting to follow you.");
        }

        return success;
    }

    // ... 其他代码保持不变

    public boolean stopAllTasks() {
        boolean success = neurorobotAI.stopAllTasks();
        setAiState(AIState.IDLE);

        // 确保完全停止所有移动
        this.getNavigation().stop();
        this.setVelocity(0, this.getVelocity().y, 0);

        if (currentCommander != null) {
            sendMessageToPlayer(currentCommander, "I've stopped all movement and will stay here.");
        }
        return success;
    }

// ... 其他代码保持不变

    public boolean mineResources(String resourceType) {
        if (currentCommander == null) return false;

        boolean success = neurorobotAI.mineResources();

        if (success) {
            setAiState(AIState.MINING);
            sendMessageToPlayer(currentCommander, "I'll search for " + resourceType + "! (Simulated for now)");
        } else {
            sendMessageToPlayer(currentCommander, "I cannot start mining right now.");
        }
        return success;
    }

    public boolean exploreArea(int radius) {
        boolean success = neurorobotAI.exploreArea(radius);
        if (success) {
            setAiState(AIState.EXPLORING);
            if (currentCommander != null) {
                sendMessageToPlayer(currentCommander, "Starting to explore the area.");
            }
        }
        return success;
    }

    public boolean gotoPosition(BlockPos position) {
        boolean success = neurorobotAI.gotoPosition(position);
        if (success) {
            setAiState(AIState.MOVING);
            if (currentCommander != null) {
                sendMessageToPlayer(currentCommander, "Moving to the specified position.");
            }
        }
        return success;
    }

    // 设置AI状态
    public void setAiState(AIState state) {
        this.aiState = state;
    }

    public String getStatusReport() {
        return String.format("Health: %.1f/%.1f | State: %s | Task: %s",
                this.getHealth(), this.getMaxHealth(),
                this.aiState.toString(),
                neurorobotAI.getCurrentGoal());
    }

    @Override
    public boolean canTarget(EntityType<?> type) {
        return super.canTarget(type);
    }

    // AI状态枚举
    public enum AIState {
        IDLE,
        FOLLOWING,
        MINING,
        EXPLORING,
        MOVING,
        WORKING
    }
}