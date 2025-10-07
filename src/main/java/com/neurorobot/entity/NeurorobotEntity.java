package com.neurorobot.entity;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.ai.goal.*;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.passive.PassiveEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;
import java.util.List;

public class NeurorobotEntity extends PassiveEntity {
    // 状态标志
    private boolean isFollowing = false;
    private boolean isMining = false;
    private boolean isAttacking = false;
    private PlayerEntity followingPlayer = null;
    private BlockPos wanderTarget = null;
    private int actionCooldown = 0;

    // 物品栏系统
    private final SimpleInventory inventory = new SimpleInventory(27);
    private int pickupCooldown = 0;

    // 自动拾取设置
    private boolean autoPickupEnabled = true;

    // 挖掘相关
    private BlockPos currentMiningTarget = null;
    private int miningProgress = 0;
    private ItemStack currentTool = ItemStack.EMPTY;

    // 在 NeurorobotEntity 类中添加字段
    private boolean temporaryPickupDisabled = false;
    private int pickupDisableTicks = 0;

    public NeurorobotEntity(EntityType<? extends PassiveEntity> entityType, World world) {
        super(entityType, world);
    }

    public static DefaultAttributeContainer.Builder createNeurorobotAttributes() {
        return MobEntity.createMobAttributes()
                .add(EntityAttributes.GENERIC_MAX_HEALTH, 20.0)
                .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.3)
                .add(EntityAttributes.GENERIC_ARMOR, 2.0)
                .add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 3.0);
    }

    // 在 NeurorobotEntity.java 的 initGoals 方法中修复类名
    @Override
    protected void initGoals() {
        // 基础行为目标
        this.goalSelector.add(0, new SwimGoal(this));
        this.goalSelector.add(1, new NeurorobotPickupGoal(this));
        this.goalSelector.add(2, new FollowPlayerGoal(this, 1.0, 2.0f, 15.0f)); // 修复：使用正确的类名 FollowPlayerGoal
        this.goalSelector.add(3, new NeurorobotMineGoal(this));
        this.goalSelector.add(4, new NeurorobotAttackGoal(this));
        this.goalSelector.add(5, new NeurorobotWanderGoal(this));
        this.goalSelector.add(6, new LookAtEntityGoal(this, PlayerEntity.class, 8.0f));
        this.goalSelector.add(7, new LookAroundGoal(this));
    }


    @Nullable
    @Override
    public PassiveEntity createChild(ServerWorld world, PassiveEntity entity) {
        return null;
    }

    // 状态管理方法
    public void startFollowing(PlayerEntity player) {
        this.stopAllActions();
        this.isFollowing = true;
        this.followingPlayer = player;
        this.getNavigation().stop();
    }

    public void startMining() {
        this.stopAllActions();
        this.isMining = true;
        this.currentMiningTarget = null;
        this.miningProgress = 0;
        this.getNavigation().stop();
    }

    public void startAttacking() {
        this.stopAllActions();
        this.isAttacking = true;
        this.getNavigation().stop();
    }

    public void stopAllActions() {
        this.isFollowing = false;
        this.isMining = false;
        this.isAttacking = false;
        this.followingPlayer = null;
        this.wanderTarget = null;
        this.currentMiningTarget = null;
        this.miningProgress = 0;
        this.currentTool = ItemStack.EMPTY;
        this.getNavigation().stop();
    }

    // 添加临时禁止自动拾取的方法
    public void disablePickupTemporarily(int ticks) {
        this.temporaryPickupDisabled = true;
        this.pickupDisableTicks = ticks;
    }

    public void enablePickup() {
        this.temporaryPickupDisabled = false;
        this.pickupDisableTicks = 0;
    }

    public boolean isPickupAllowed() {
        return this.autoPickupEnabled && !this.temporaryPickupDisabled;
    }



    // 自动拾取控制方法
    public void enableAutoPickup() {
        this.autoPickupEnabled = true;
    }

    public void disableAutoPickup() {
        this.autoPickupEnabled = false;
    }

    public boolean isAutoPickupEnabled() {
        return this.autoPickupEnabled;
    }

    // 状态获取方法
    public boolean isFollowing() {
        return this.isFollowing;
    }

    public boolean isMining() {
        return this.isMining;
    }

    public boolean isAttacking() {
        return this.isAttacking;
    }

    public PlayerEntity getFollowingPlayer() {
        return this.followingPlayer;
    }

    // 物品栏管理方法
    public SimpleInventory getInventory() {
        return this.inventory;
    }

    public boolean pickupItem(ItemStack stack) {
        // 检查是否为绑定诅咒物品
        if (hasBindingCurse(stack)) {
            return false;
        }

        // 修复：在1.21中，addStack返回ItemStack而不是boolean
        ItemStack remainder = this.inventory.addStack(stack);
        // 如果剩余堆栈为空，表示所有物品都被添加了
        return remainder.isEmpty() || remainder.getCount() < stack.getCount();
    }

    // 修复丢弃物品方法
    public void dropAllItems() {
        for (int i = 0; i < this.inventory.size(); i++) {
            if (!this.inventory.getStack(i).isEmpty()) {
                dropItem(i);
            }
        }
    }

    // 修复：丢弃指定物品类型
    public void dropItemType(String itemName) {
        for (int i = 0; i < this.inventory.size(); i++) {
            ItemStack stack = this.inventory.getStack(i);
            if (!stack.isEmpty() && stack.getName().getString().toLowerCase().contains(itemName.toLowerCase())) {
                dropItem(i);
                break;
            }
        }
    }

    // 在 NeurorobotEntity.java 中更新 getInventorySummary 方法
    public Text getInventorySummary() {
        StringBuilder summary = new StringBuilder("Inventory Contents:\n");
        boolean hasItems = false;
        int itemCount = 0;

        for (int i = 0; i < this.inventory.size(); i++) {
            ItemStack stack = this.inventory.getStack(i);
            if (!stack.isEmpty()) {
                hasItems = true;
                itemCount++;

                // 添加槽位编号和物品信息
                summary.append("Slot ").append(i).append(": ");
                summary.append(stack.getCount()).append("x ").append(stack.getName().getString());

                // 如果是工具，显示耐久度
                if (stack.getMaxDamage() > 0) {
                    int damage = stack.getDamage();
                    int maxDamage = stack.getMaxDamage();
                    int remaining = maxDamage - damage;
                    summary.append(" [Durability: ").append(remaining).append("/").append(maxDamage).append("]");
                }

                summary.append("\n");
            }
        }

        if (!hasItems) {
            summary.append("Empty");
        } else {
            summary.append("Total items: ").append(itemCount);
        }

        return Text.literal(summary.toString());
    }


    // 修复工具选择逻辑
    public ItemStack getBestToolForBlock(net.minecraft.block.BlockState state) {
        float bestSpeed = 1.0f; // 空手速度
        ItemStack bestTool = ItemStack.EMPTY;

        for (int i = 0; i < this.inventory.size(); i++) {
            ItemStack stack = this.inventory.getStack(i);
            if (!stack.isEmpty()) {
                // 检查工具是否适合挖掘该方块
                if (isToolEffectiveForBlock(stack, state)) {
                    float speed = getMiningSpeed(stack, state);
                    if (speed > bestSpeed) {
                        bestSpeed = speed;
                        bestTool = stack;
                    }
                }
            }
        }

        return bestTool;
    }

    // 检查工具是否对特定方块有效
    private boolean isToolEffectiveForBlock(ItemStack tool, net.minecraft.block.BlockState state) {
        // 简单的工具有效性检查
        if (tool.getItem() instanceof net.minecraft.item.PickaxeItem) {
            return state.isIn(net.minecraft.registry.tag.BlockTags.PICKAXE_MINEABLE);
        } else if (tool.getItem() instanceof net.minecraft.item.AxeItem) {
            return state.isIn(net.minecraft.registry.tag.BlockTags.AXE_MINEABLE);
        } else if (tool.getItem() instanceof net.minecraft.item.ShovelItem) {
            return state.isIn(net.minecraft.registry.tag.BlockTags.SHOVEL_MINEABLE);
        }
        return false;
    }

    // 获取挖掘速度
    private float getMiningSpeed(ItemStack tool, net.minecraft.block.BlockState state) {
        float speed = tool.getMiningSpeedMultiplier(state);

        // 如果没有速度加成，返回空手速度
        if (speed <= 1.0f) {
            return 1.0f;
        }

        return speed;
    }

    // 实际挖掘方块的方法


    // 寻找可挖掘的方块
    public BlockPos findMineableBlock() {
        BlockPos entityPos = this.getBlockPos();

        // 在周围寻找可挖掘的方块
        for (int x = -3; x <= 3; x++) {
            for (int y = -1; y <= 2; y++) {
                for (int z = -3; z <= 3; z++) {
                    BlockPos checkPos = entityPos.add(x, y, z);
                    net.minecraft.block.BlockState state = this.getWorld().getBlockState(checkPos);

                    if (isMineableBlock(state) && !state.isAir()) {
                        return checkPos;
                    }
                }
            }
        }
        return null;
    }

    private boolean isMineableBlock(net.minecraft.block.BlockState state) {
        return state.isIn(net.minecraft.registry.tag.BlockTags.PICKAXE_MINEABLE) ||
                state.isIn(net.minecraft.registry.tag.BlockTags.AXE_MINEABLE) ||
                state.isIn(net.minecraft.registry.tag.BlockTags.SHOVEL_MINEABLE) ||
                state.isOf(net.minecraft.block.Blocks.STONE) ||
                state.isOf(net.minecraft.block.Blocks.COBBLESTONE) ||
                state.isOf(net.minecraft.block.Blocks.DIRT) ||
                state.isOf(net.minecraft.block.Blocks.GRASS_BLOCK) ||
                state.isOf(net.minecraft.block.Blocks.OAK_LOG) ||
                state.isOf(net.minecraft.block.Blocks.COAL_ORE) ||
                state.isOf(net.minecraft.block.Blocks.IRON_ORE) ||
                state.isOf(net.minecraft.block.Blocks.GOLD_ORE) ||
                state.isOf(net.minecraft.block.Blocks.DIAMOND_ORE);
    }

    private boolean hasBindingCurse(ItemStack stack) {
        return stack.hasEnchantments() &&
                stack.getEnchantments().toString().contains("binding_curse");
    }

    // 调试方法
//    public void debugStatus() {
//        if (!this.getWorld().isClient()) {
//            String status = String.format(
//                    "Following: %b, Mining: %b, Attacking: %b, AutoPickup: %b, Player: %s, Items: %d, MiningTarget: %s",
//                    this.isFollowing, this.isMining, this.isAttacking, this.autoPickupEnabled,
//                    this.followingPlayer != null ? this.followingPlayer.getName().getString() : "null",
//                    getItemCount(),
//                    this.currentMiningTarget != null ? this.currentMiningTarget.toString() : "null"
//            );
//            this.getServer().getPlayerManager().broadcast(
//                    createDebugMessage(status),
//                    false
//            );
//        }
//    }

    // 在 NeurorobotEntity 的 debugStatus 方法中添加拾取状态
    public void debugStatus() {
        if (!this.getWorld().isClient()) {
            String status = String.format(
                    "Following: %b, Mining: %b, Attacking: %b, AutoPickup: %b, TempDisabled: %b, Player: %s, Items: %d, MiningTarget: %s",
                    this.isFollowing, this.isMining, this.isAttacking, this.autoPickupEnabled, this.temporaryPickupDisabled,
                    this.followingPlayer != null ? this.followingPlayer.getName().getString() : "null",
                    getItemCount(),
                    this.currentMiningTarget != null ? this.currentMiningTarget.toString() : "null"
            );
            this.getServer().getPlayerManager().broadcast(
                    createDebugMessage(status),
                    false
            );
        }
    }


    private int getItemCount() {
        int count = 0;
        for (int i = 0; i < this.inventory.size(); i++) {
            if (!this.inventory.getStack(i).isEmpty()) {
                count++;
            }
        }
        return count;
    }

    private static Text createDebugMessage(String message) {
        MutableText prefix = Text.literal("[DEBUG] ")
                .setStyle(Style.EMPTY.withColor(Formatting.RED).withBold(true));
        MutableText content = Text.literal(message)
                .setStyle(Style.EMPTY.withColor(Formatting.WHITE).withBold(false));

        return prefix.append(content);
    }

    private void updateBehavior() {
        // 如果正在挖掘但没有目标，寻找新目标
        if (this.isMining && this.currentMiningTarget == null) {
            this.currentMiningTarget = findMineableBlock();
            if (this.currentMiningTarget != null) {
                this.getNavigation().startMovingTo(
                        this.currentMiningTarget.getX() + 0.5,
                        this.currentMiningTarget.getY(),
                        this.currentMiningTarget.getZ() + 0.5,
                        1.0
                );
            }
        }

        // 如果没有任何活动状态，随机选择一个行为
        if (!this.isFollowing && !this.isMining && !this.isAttacking) {
            if (this.getRandom().nextFloat() < 0.1f) {
                if (this.getRandom().nextBoolean()) {
                    this.startMining();
                } else {
                    double x = this.getX() + (this.getRandom().nextDouble() - 0.5) * 10;
                    double z = this.getZ() + (this.getRandom().nextDouble() - 0.5) * 10;
                    this.wanderTarget = new BlockPos((int)x, (int)this.getY(), (int)z);
                }
            }
        }
    }

    // NBT数据保存和加载
    @Override
    public void writeCustomDataToNbt(NbtCompound nbt) {
        super.writeCustomDataToNbt(nbt);

        // 保存物品栏
        NbtList inventoryList = new NbtList();
        for (int i = 0; i < this.inventory.size(); i++) {
            ItemStack stack = this.inventory.getStack(i);
            if (!stack.isEmpty()) {
                NbtCompound itemNbt = new NbtCompound();
                itemNbt.putByte("Slot", (byte)i);

                // 简化：只保存物品ID和数量
                itemNbt.putString("id", net.minecraft.registry.Registries.ITEM.getId(stack.getItem()).toString());
                itemNbt.putInt("Count", stack.getCount());
                itemNbt.putInt("Damage", stack.getDamage());

                inventoryList.add(itemNbt);
            }
        }
        nbt.put("Inventory", inventoryList);

        // 保存自动拾取设置
        nbt.putBoolean("AutoPickup", this.autoPickupEnabled);
    }

    @Override
    public void readCustomDataFromNbt(NbtCompound nbt) {
        super.readCustomDataFromNbt(nbt);

        // 加载物品栏
        if (nbt.contains("Inventory", 9)) {
            NbtList inventoryList = nbt.getList("Inventory", 10);
            for (int i = 0; i < inventoryList.size(); i++) {
                NbtCompound itemNbt = inventoryList.getCompound(i);
                int slot = itemNbt.getByte("Slot") & 255;
                if (slot >= 0 && slot < this.inventory.size()) {
                    // 从物品ID和数量重建物品堆栈
                    String itemId = itemNbt.getString("id");
                    int count = itemNbt.getInt("Count");
                    int damage = itemNbt.getInt("Damage");

                    net.minecraft.util.Identifier identifier = net.minecraft.util.Identifier.tryParse(itemId);
                    if (identifier != null) {
                        net.minecraft.item.Item item = net.minecraft.registry.Registries.ITEM.get(identifier);
                        if (item != null) {
                            ItemStack stack = new ItemStack(item, count);
                            stack.setDamage(damage);
                            this.inventory.setStack(slot, stack);
                        }
                    }
                }
            }
        }

        // 加载自动拾取设置
        if (nbt.contains("AutoPickup")) {
            this.autoPickupEnabled = nbt.getBoolean("AutoPickup");
        }
    }

    // 跟随目标
    // 在 NeurorobotEntity.java 中检查 FollowPlayerGoal
    static class FollowPlayerGoal extends Goal {
        private final NeurorobotEntity neurorobot;
        private final double speed;
        private final float minDistance;
        private final float maxDistance;
        private int updateCountdownTicks;

        public FollowPlayerGoal(NeurorobotEntity neurorobot, double speed, float minDistance, float maxDistance) {
            this.neurorobot = neurorobot;
            this.speed = speed;
            this.minDistance = minDistance;
            this.maxDistance = maxDistance;
            this.setControls(EnumSet.of(Goal.Control.MOVE, Goal.Control.LOOK));
        }

        @Override
        public boolean canStart() {
            // 确保跟随状态和玩家存在
            if (!this.neurorobot.isFollowing() || this.neurorobot.getFollowingPlayer() == null) {
                return false;
            }

            // 检查玩家是否在范围内
            double distanceSquared = this.neurorobot.squaredDistanceTo(this.neurorobot.getFollowingPlayer());
            if (distanceSquared > (double)(this.maxDistance * this.maxDistance)) {
                return false;
            }

            // 只有当距离大于最小距离时才开始跟随
            return distanceSquared > (double)(this.minDistance * this.minDistance);
        }

        @Override
        public void start() {
            this.updateCountdownTicks = 0;
            PlayerEntity player = this.neurorobot.getFollowingPlayer();
            if (player != null) {
                // 立即开始移动到玩家
                this.neurorobot.getNavigation().startMovingTo(player, this.speed);
            }
        }

        @Override
        public void tick() {
            PlayerEntity player = this.neurorobot.getFollowingPlayer();
            if (player == null) {
                return;
            }

            // 持续看向玩家
            this.neurorobot.getLookControl().lookAt(player, 10.0f, (float)this.neurorobot.getMaxLookPitchChange());

            // 定期更新路径
            if (--this.updateCountdownTicks <= 0) {
                this.updateCountdownTicks = 10;

                // 如果导航停止或玩家移动，重新开始移动
                if (this.neurorobot.getNavigation().isIdle() ||
                        this.neurorobot.squaredDistanceTo(player) > (double)(this.minDistance * this.minDistance)) {
                    this.neurorobot.getNavigation().startMovingTo(player, this.speed);
                }
            }
        }

        @Override
        public boolean shouldContinue() {
            // 只要跟随状态为真且玩家存在，就继续跟随
            return this.neurorobot.isFollowing() &&
                    this.neurorobot.getFollowingPlayer() != null;
        }

        @Override
        public void stop() {
            this.neurorobot.getNavigation().stop();
        }
    }


    // 挖掘目标 - 重新设计
    static class NeurorobotMineGoal extends Goal {
        private final NeurorobotEntity neurorobot;

        public NeurorobotMineGoal(NeurorobotEntity neurorobot) {
            this.neurorobot = neurorobot;
            this.setControls(EnumSet.of(Goal.Control.MOVE, Goal.Control.LOOK));
        }

        @Override
        public boolean canStart() {
            return this.neurorobot.isMining() && this.neurorobot.currentMiningTarget == null;
        }

        @Override
        public void start() {
            // 寻找可挖掘的方块
            BlockPos target = this.neurorobot.findMineableBlock();
            if (target != null) {
                this.neurorobot.currentMiningTarget = target;
                this.neurorobot.getNavigation().startMovingTo(
                        target.getX() + 0.5,
                        target.getY(),
                        target.getZ() + 0.5,
                        1.0
                );
            }
        }

        @Override
        public void tick() {
            // 挖掘逻辑现在在实体的tick方法中处理
        }

        @Override
        public boolean shouldContinue() {
            return this.neurorobot.isMining();
        }

        @Override
        public void stop() {
            this.neurorobot.currentMiningTarget = null;
            this.neurorobot.miningProgress = 0;
        }
    }

    // 攻击目标
    static class NeurorobotAttackGoal extends Goal {
        private final NeurorobotEntity neurorobot;
        private net.minecraft.entity.mob.MobEntity target;

        public NeurorobotAttackGoal(NeurorobotEntity neurorobot) {
            this.neurorobot = neurorobot;
            this.setControls(EnumSet.of(Goal.Control.MOVE, Goal.Control.LOOK));
        }

        @Override
        public boolean canStart() {
            return this.neurorobot.isAttacking() && findAttackTarget();
        }

        @Override
        public void start() {
            if (this.target != null) {
                this.neurorobot.getNavigation().startMovingTo(this.target, 1.0);
            }
        }

        @Override
        public void tick() {
            if (this.target == null || !this.target.isAlive()) {
                findAttackTarget();
                return;
            }

            this.neurorobot.getLookControl().lookAt(this.target);

            double distance = this.neurorobot.squaredDistanceTo(this.target);
            if (distance <= 16.0) {
                if (this.neurorobot.actionCooldown <= 0) {
                    this.neurorobot.tryAttack(this.target);
                    this.neurorobot.actionCooldown = 20;
                }
            } else {
                this.neurorobot.getNavigation().startMovingTo(this.target, 1.0);
            }
        }

        @Override
        public boolean shouldContinue() {
            return this.neurorobot.isAttacking() &&
                    this.target != null &&
                    this.target.isAlive();
        }

        @Override
        public void stop() {
            this.target = null;
        }

        private boolean findAttackTarget() {
            List<net.minecraft.entity.mob.MobEntity> monsters = this.neurorobot.getWorld().getEntitiesByClass(
                    net.minecraft.entity.mob.MobEntity.class,
                    this.neurorobot.getBoundingBox().expand(10.0),
                    entity -> entity.isAlive() &&
                            entity != this.neurorobot &&
                            this.neurorobot.canSee(entity)
            );

            if (!monsters.isEmpty()) {
                this.target = monsters.get(0);
                return true;
            }
            return false;
        }
    }

    // 拾取物品目标
    static class NeurorobotPickupGoal extends Goal {
        private final NeurorobotEntity neurorobot;
        private net.minecraft.entity.ItemEntity targetItem;

        public NeurorobotPickupGoal(NeurorobotEntity neurorobot) {
            this.neurorobot = neurorobot;
            this.setControls(EnumSet.of(Goal.Control.MOVE));
        }

//        @Override
//        public boolean canStart() {
//            // 检查自动拾取是否启用且冷却结束
//            if (!this.neurorobot.autoPickupEnabled || this.neurorobot.pickupCooldown > 0) {
//                return false;
//            }
//
//            // 检查是否有可拾取的物品
//            return findPickupTarget();
//        }

        // 在 NeurorobotPickupGoal 中更新 canStart 方法
//        @Override
//        public boolean canStart() {
//            // 检查自动拾取是否启用且冷却结束，并且没有临时禁止
//            if (!this.neurorobot.isPickupAllowed() || this.neurorobot.pickupCooldown > 0) {
//                return false;
//            }
//
//            // 检查是否有可拾取的物品
//            return findPickupTarget();
//        }

        @Override
        public boolean canStart() {
            // 检查自动拾取是否启用且冷却结束，并且没有临时禁止
            if (!this.neurorobot.isPickupAllowed() || this.neurorobot.pickupCooldown > 0) {
                System.out.println("[DEBUG] Pickup goal cannot start: pickup not allowed or on cooldown");
                return false;
            }

            // 检查是否有可拾取的物品
            return findPickupTarget();
        }


        @Override
        public void start() {
            if (this.targetItem != null) {
                // 移动到物品位置
                this.neurorobot.getNavigation().startMovingTo(
                        this.targetItem.getX(),
                        this.targetItem.getY(),
                        this.targetItem.getZ(),
                        1.2
                );
            }
        }

        @Override
        public void tick() {
            if (this.targetItem == null || !this.targetItem.isAlive()) {
                return;
            }

            // 检查是否靠近物品
            double distance = this.neurorobot.squaredDistanceTo(this.targetItem);
            if (distance <= 2.0) {
                // 尝试拾取物品
                if (this.neurorobot.pickupItem(this.targetItem.getStack())) {
                    this.targetItem.discard();
                    this.neurorobot.pickupCooldown = 10; // 拾取冷却
                }
                this.targetItem = null;
            } else if (this.neurorobot.getNavigation().isIdle()) {
                // 如果导航停止，重新开始移动
                this.neurorobot.getNavigation().startMovingTo(
                        this.targetItem.getX(),
                        this.targetItem.getY(),
                        this.targetItem.getZ(),
                        1.2
                );
            }
        }

        @Override
        public boolean shouldContinue() {
            return this.neurorobot.autoPickupEnabled &&
                    this.targetItem != null &&
                    this.targetItem.isAlive() &&
                    this.neurorobot.pickupCooldown <= 0;
        }

        @Override
        public void stop() {
            this.targetItem = null;
            this.neurorobot.getNavigation().stop();
        }

        private boolean findPickupTarget() {
            // 寻找附近的物品
            List<net.minecraft.entity.ItemEntity> items = this.neurorobot.getWorld().getEntitiesByClass(
                    net.minecraft.entity.ItemEntity.class,
                    this.neurorobot.getBoundingBox().expand(8.0), // 8格范围内的物品
                    item -> item.isAlive() && !hasBindingCurse(item.getStack())
            );

            if (!items.isEmpty()) {
                // 选择最近的物品
                this.targetItem = items.get(0);
                return true;
            }
            return false;
        }

        private boolean hasBindingCurse(ItemStack stack) {
            return stack.hasEnchantments() &&
                    stack.getEnchantments().toString().contains("binding_curse");
        }
    }

    // 漫步目标
    static class NeurorobotWanderGoal extends Goal {
        private final NeurorobotEntity neurorobot;

        public NeurorobotWanderGoal(NeurorobotEntity neurorobot) {
            this.neurorobot = neurorobot;
            this.setControls(EnumSet.of(Goal.Control.MOVE));
        }

        @Override
        public boolean canStart() {
            return !this.neurorobot.isFollowing() &&
                    !this.neurorobot.isMining() &&
                    !this.neurorobot.isAttacking() &&
                    this.neurorobot.getRandom().nextFloat() < 0.02f;
        }

        @Override
        public void start() {
            double x = this.neurorobot.getX() + (this.neurorobot.getRandom().nextDouble() - 0.5) * 10;
            double z = this.neurorobot.getZ() + (this.neurorobot.getRandom().nextDouble() - 0.5) * 10;
            this.neurorobot.getNavigation().startMovingTo(x, this.neurorobot.getY(), z, 0.8);
        }

        @Override
        public boolean shouldContinue() {
            return !this.neurorobot.getNavigation().isIdle() &&
                    !this.neurorobot.isFollowing() &&
                    !this.neurorobot.isMining() &&
                    !this.neurorobot.isAttacking();
        }
    }

    // 手持物品
    private ItemStack mainHandStack = ItemStack.EMPTY;

    // 获取和设置手持物品的方法
    public ItemStack getMainHandStack() {
        return this.mainHandStack;
    }

    public void setMainHandStack(ItemStack stack) {
        this.mainHandStack = stack;
    }

    // 在挖掘时更新手持工具
    // 在 NeurorobotEntity.java 中更新 updateHeldTool 方法
    public void updateHeldTool() {
        if (this.isMining && this.currentMiningTarget != null) {
            net.minecraft.block.BlockState state = this.getWorld().getBlockState(this.currentMiningTarget);
            ItemStack bestTool = getBestToolForBlock(state);

            // 只有当找到新工具时才更新手持物品
            if (!bestTool.isEmpty() && bestTool != this.mainHandStack) {
                this.setMainHandStack(bestTool);
                return;
            }
        }

        // 如果没有挖掘或者没有找到工具，但在挖掘状态中，保持当前工具
        if (!this.isMining) {
            this.setMainHandStack(ItemStack.EMPTY);
        }
    }


    // 在tick方法中更新手持工具
//    @Override
//    public void tick() {
//        super.tick();
//
//        // 处理动作冷却
//        if (this.actionCooldown > 0) {
//            this.actionCooldown--;
//        }
//
//        // 处理拾取冷却
//        if (this.pickupCooldown > 0) {
//            this.pickupCooldown--;
//        }
//
//        // 更新手持工具
//        if (this.age % 10 == 0) { // 每10tick更新一次
//            updateHeldTool();
//        }
//
//        // 处理挖掘逻辑
//        if (this.isMining && this.currentMiningTarget != null) {
//            // 看向目标方块
//            this.getLookControl().lookAt(
//                    this.currentMiningTarget.getX() + 0.5,
//                    this.currentMiningTarget.getY() + 0.5,
//                    this.currentMiningTarget.getZ() + 0.5
//            );
//
//            // 检查是否靠近方块
//            double distance = this.squaredDistanceTo(
//                    this.currentMiningTarget.getX() + 0.5,
//                    this.currentMiningTarget.getY(),
//                    this.currentMiningTarget.getZ() + 0.5
//            );
//
//            if (distance <= 4.0) {
//                this.getNavigation().stop();
//                mineBlock(this.currentMiningTarget);
//            }
//        }
//
//        // 定期检查状态并更新行为
//        if (this.age % 20 == 0) {
//            updateBehavior();
//        }
//    }

    // 在 NeurorobotEntity 的 tick 方法中添加临时禁止处理
    @Override
    public void tick() {
        super.tick();

        // 处理动作冷却
        if (this.actionCooldown > 0) {
            this.actionCooldown--;
        }

        // 处理拾取冷却
        if (this.pickupCooldown > 0) {
            this.pickupCooldown--;
        }

        // 处理临时禁止拾取
        if (this.temporaryPickupDisabled && this.pickupDisableTicks > 0) {
            this.pickupDisableTicks--;
            if (this.pickupDisableTicks <= 0) {
                this.temporaryPickupDisabled = false;
            }
        }

        // 更新手持工具
        if (this.age % 10 == 0) {
            updateHeldTool();
        }

        // 处理挖掘逻辑
        if (this.isMining && this.currentMiningTarget != null) {
            // 看向目标方块
            this.getLookControl().lookAt(
                    this.currentMiningTarget.getX() + 0.5,
                    this.currentMiningTarget.getY() + 0.5,
                    this.currentMiningTarget.getZ() + 0.5
            );

            // 检查是否靠近方块
            double distance = this.squaredDistanceTo(
                    this.currentMiningTarget.getX() + 0.5,
                    this.currentMiningTarget.getY(),
                    this.currentMiningTarget.getZ() + 0.5
            );

            if (distance <= 4.0) {
                this.getNavigation().stop();
                mineBlock(this.currentMiningTarget);
            }
        }

        // 定期检查状态并更新行为
        if (this.age % 20 == 0) {
            updateBehavior();
        }
    }


    // 修改丢弃物品方法 - 像玩家一样丢弃
    // 在 NeurorobotEntity.java 中完全重写 dropItem 方法
    // 在 NeurorobotEntity.java 中修复 dropItem 方法
//    public void dropItem(int slot) {
//        if (slot >= 0 && slot < this.inventory.size()) {
//            ItemStack stack = this.inventory.getStack(slot);
//            if (!stack.isEmpty() && !this.getWorld().isClient()) {
//
//                // 创建物品实体
//                net.minecraft.entity.ItemEntity itemEntity = new net.minecraft.entity.ItemEntity(
//                        this.getWorld(),
//                        this.getX(),
//                        this.getY() + this.getEyeHeight(this.getPose()),
//                        this.getZ(),
//                        stack.copy()
//                );
//
//                // 计算丢弃方向（基于视线方向）
//                Vec3d lookVec = this.getRotationVec(1.0F);
//                float yawRadians = this.getYaw() * 0.017453292F;
//                float pitchRadians = this.getPitch() * 0.017453292F;
//
//                // 更真实的丢弃速度计算
//                double speed = 0.3;
//                itemEntity.setVelocity(
//                        -Math.sin(yawRadians) * Math.cos(pitchRadians) * speed,
//                        -Math.sin(pitchRadians) * speed + 0.1,
//                        Math.cos(yawRadians) * Math.cos(pitchRadians) * speed
//                );
//
//                // 设置随机旋转和拾取延迟
//                itemEntity.setPickupDelay(40);
//
//                // 修复：在1.21中，setThrower方法已被移除或更改
//                // 我们使用setOwner方法来设置物品的所有者
//                itemEntity.setOwner(this.getUuid());
//
//                // 生成物品实体
//                this.getWorld().spawnEntity(itemEntity);
//
//                // 从物品栏移除
//                this.inventory.setStack(slot, ItemStack.EMPTY);
//
//                // 如果丢弃的是手持物品，清空手持物品
//                if (stack == this.mainHandStack) {
//                    this.setMainHandStack(ItemStack.EMPTY);
//                }
//            }
//        }
//    }

    // 在 NeurorobotEntity.java 中更新 dropItem 方法，添加调试信息
    // 在 NeurorobotEntity.java 中更新现有的 dropItem 方法
    public void dropItem(int slot) {
        if (slot >= 0 && slot < this.inventory.size()) {
            ItemStack stack = this.inventory.getStack(slot);
            if (!stack.isEmpty() && !this.getWorld().isClient()) {

                // 临时禁止拾取 10 秒 (200 ticks)
                this.disablePickupTemporarily(200);
                System.out.println("[DEBUG] Drop item - temporarily disabling pickup");

                // 创建物品实体
                net.minecraft.entity.ItemEntity itemEntity = new net.minecraft.entity.ItemEntity(
                        this.getWorld(),
                        this.getX(),
                        this.getY() + this.getEyeHeight(this.getPose()),
                        this.getZ(),
                        stack.copy()
                );

                // 计算丢弃方向（基于视线方向）
                Vec3d lookVec = this.getRotationVec(1.0F);
                float yawRadians = this.getYaw() * 0.017453292F;
                float pitchRadians = this.getPitch() * 0.017453292F;

                // 更真实的丢弃速度计算
                double speed = 0.3;
                itemEntity.setVelocity(
                        -Math.sin(yawRadians) * Math.cos(pitchRadians) * speed,
                        -Math.sin(pitchRadians) * speed + 0.1,
                        Math.cos(yawRadians) * Math.cos(pitchRadians) * speed
                );

                // 设置随机旋转和拾取延迟
                itemEntity.setPickupDelay(40);

                // 生成物品实体
                boolean spawned = this.getWorld().spawnEntity(itemEntity);

                if (spawned) {
                    // 从物品栏移除
                    this.inventory.setStack(slot, ItemStack.EMPTY);

                    // 如果丢弃的是手持物品，清空手持物品
                    if (stack == this.mainHandStack) {
                        this.setMainHandStack(ItemStack.EMPTY);
                    }

                    System.out.println("[DEBUG] Successfully dropped item: " + stack.getName().getString());
                } else {
                    System.out.println("[DEBUG] FAILED to spawn ItemEntity!");
                }
            }
        }
    }


    // 修改挖掘方法以更新工具显示
    public boolean mineBlock(BlockPos pos) {
        if (!this.getWorld().isClient() && this.isMining) {
            net.minecraft.block.BlockState state = this.getWorld().getBlockState(pos);

            // 检查是否为可挖掘的方块
            if (!isMineableBlock(state)) {
                return false;
            }

            // 选择最佳工具并更新手持显示
            this.currentTool = getBestToolForBlock(state);
            if (!this.currentTool.isEmpty()) {
                this.setMainHandStack(this.currentTool);
            }

            // 增加挖掘进度
            this.miningProgress++;

            float hardness = state.getHardness(this.getWorld(), pos);
            int requiredProgress = (int) (hardness * 20); // 将硬度转换为挖掘进度

            if (this.miningProgress >= requiredProgress) {
                // 破坏方块
                boolean success = this.getWorld().breakBlock(pos, true, this);
                if (success) {
                    // 减少工具耐久度
                    if (!this.currentTool.isEmpty()) {
                        int newDamage = this.currentTool.getDamage() + 1;
                        if (newDamage < this.currentTool.getMaxDamage()) {
                            this.currentTool.setDamage(newDamage);
                        } else {
                            // 工具损坏
                            this.currentTool = ItemStack.EMPTY;
                            this.setMainHandStack(ItemStack.EMPTY);
                        }
                    }

                    this.miningProgress = 0;
                    this.currentMiningTarget = null;
                    return true;
                }
            }
        }
        return false;
    }
}
