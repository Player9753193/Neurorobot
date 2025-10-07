package com.neurorobot.event;

import com.neurorobot.entity.NeurorobotEntity;
import net.fabricmc.fabric.api.message.v1.ServerMessageEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.List;

public class ChatListener {

    public static void register() {
        ServerMessageEvents.CHAT_MESSAGE.register((message, sender, params) -> {
            String text = message.getContent().getString().toLowerCase();
            ServerPlayerEntity player = sender;

            // 检查是否以Neurorobot或神经机器人开头
            boolean isCommandForNeurorobot = false;
            String commandText = "";

            if (text.startsWith("neurorobot")) {
                isCommandForNeurorobot = true;
                commandText = text.substring("neurorobot".length()).trim();
            } else if (text.startsWith("神经机器人")) {
                isCommandForNeurorobot = true;
                commandText = text.substring("神经机器人".length()).trim();
            }

            // 如果不是对Neurorobot的命令，跳过处理
            if (!isCommandForNeurorobot) {
                return;
            }

            // 查找10格内的Neurorobot
            List<NeurorobotEntity> neurorobots = player.getServerWorld().getEntitiesByClass(
                    NeurorobotEntity.class,
                    player.getBoundingBox().expand(10.0),
                    entity -> true
            );

            if (neurorobots.isEmpty()) {
                player.sendMessage(createNeurorobotMessage("I'm not nearby. Please summon me first."), false);
                return;
            }

            NeurorobotEntity neurorobot = neurorobots.get(0);

            // 处理各种命令
            if (commandText.isEmpty() || commandText.contains("hello") || commandText.contains("hi") || commandText.contains("你好")) {
                player.getServer().getPlayerManager().broadcast(
                        createNeurorobotMessage("Hello, " + player.getName().getString() + "! How can I help you?"),
                        false
                );
            }
            // 在 ChatListener.java 中检查跟随命令处理
            // 在 ChatListener.java 中更新跟随命令
            else if (commandText.contains("follow") || commandText.contains("跟随")) {
                neurorobot.startFollowing(player);

                // 添加调试信息
                String debugInfo = String.format(
                        "I will follow you, %s. Following: %b, Player: %s",
                        player.getName().getString(),
                        neurorobot.isFollowing(),
                        neurorobot.getFollowingPlayer() != null ? neurorobot.getFollowingPlayer().getName().getString() : "null"
                );

                player.getServer().getPlayerManager().broadcast(
                        createNeurorobotMessage(debugInfo),
                        false
                );
            }
            else if (commandText.contains("stop") || commandText.contains("停止")) {
                neurorobot.stopAllActions();
                player.getServer().getPlayerManager().broadcast(
                        createNeurorobotMessage("I've stopped all actions."),
                        false
                );
            }
            else if (commandText.contains("mine") || commandText.contains("挖掘")) {
                neurorobot.startMining();
                player.getServer().getPlayerManager().broadcast(
                        createNeurorobotMessage("I will start mining nearby blocks."),
                        false
                );
            }
            else if (commandText.contains("attack") || commandText.contains("攻击")) {
                neurorobot.startAttacking();
                player.getServer().getPlayerManager().broadcast(
                        createNeurorobotMessage("I will attack nearby monsters."),
                        false
                );
            }
            else if (commandText.contains("inventory") || commandText.contains("背包")) {
                Text inventorySummary = neurorobot.getInventorySummary();
                player.getServer().getPlayerManager().broadcast(
                        createNeurorobotMessage(inventorySummary.getString()),
                        false
                );
            }
            // 在ChatListener.java中更新丢弃命令部分
            else if (commandText.contains("drop all") || commandText.contains("丢弃全部")) {
                neurorobot.dropAllItems();
                player.getServer().getPlayerManager().broadcast(
                        createNeurorobotMessage("I've dropped all items from my inventory."),
                        false
                );
            }
            else if (commandText.contains("drop") || commandText.contains("丢弃")) {
                // 处理丢弃特定物品
                String[] parts = commandText.split(" ");
                if (parts.length > 1) {
                    // 丢弃指定类型的物品
                    neurorobot.dropItemType(parts[1]);
                    player.getServer().getPlayerManager().broadcast(
                            createNeurorobotMessage("I've dropped the requested item."),
                            false
                    );
                } else {
                    // 丢弃第一个非空槽位的物品
                    boolean dropped = false;
                    for (int i = 0; i < neurorobot.getInventory().size(); i++) {
                        if (!neurorobot.getInventory().getStack(i).isEmpty()) {
                            neurorobot.dropItem(i);
                            dropped = true;
                            break;
                        }
                    }

                    if (dropped) {
                        player.getServer().getPlayerManager().broadcast(
                                createNeurorobotMessage("I've dropped an item."),
                                false
                        );
                    } else {
                        player.getServer().getPlayerManager().broadcast(
                                createNeurorobotMessage("My inventory is empty."),
                                false
                        );
                    }
                }
            }
            else if (commandText.contains("pickup") || commandText.contains("拾取")) {
                // 强制尝试拾取物品
                List<net.minecraft.entity.ItemEntity> items = player.getServerWorld().getEntitiesByClass(
                        net.minecraft.entity.ItemEntity.class,
                        neurorobot.getBoundingBox().expand(5.0),
                        item -> item.isAlive()
                );

                if (!items.isEmpty()) {
                    boolean pickedUp = false;
                    for (net.minecraft.entity.ItemEntity item : items) {
                        if (neurorobot.pickupItem(item.getStack())) {
                            item.discard();
                            pickedUp = true;
                            break;
                        }
                    }

                    if (pickedUp) {
                        player.getServer().getPlayerManager().broadcast(
                                createNeurorobotMessage("I picked up an item."),
                                false
                        );
                    } else {
                        player.getServer().getPlayerManager().broadcast(
                                createNeurorobotMessage("I couldn't pick up any items."),
                                false
                        );
                    }
                } else {
                    player.getServer().getPlayerManager().broadcast(
                            createNeurorobotMessage("No items nearby to pick up."),
                            false
                    );
                }
            }
            else if (commandText.contains("auto collect on") || commandText.contains("自动拾取开启")) {
                neurorobot.enableAutoPickup();
                player.getServer().getPlayerManager().broadcast(
                        createNeurorobotMessage("Auto pickup enabled."),
                        false
                );
            }
            else if (commandText.contains("auto collect off") || commandText.contains("自动拾取关闭")) {
                neurorobot.disableAutoPickup();
                player.getServer().getPlayerManager().broadcast(
                        createNeurorobotMessage("Auto pickup disabled."),
                        false
                );
            }
            else if (commandText.contains("collectable") || commandText.contains("自动拾取")) {
                boolean status = neurorobot.isAutoPickupEnabled();
                player.getServer().getPlayerManager().broadcast(
                        createNeurorobotMessage("Auto collect is " + (status ? "enabled" : "disabled") + "."),
                        false
                );
            }
            else if (commandText.contains("enable pickup") || commandText.contains("允许拾取")) {
                neurorobot.enablePickup();
                player.getServer().getPlayerManager().broadcast(
                        createNeurorobotMessage("Auto pickup is now enabled."),
                        false
                );
            }
            else if (commandText.contains("pickup status") || commandText.contains("拾取状态")) {
                boolean autoEnabled = neurorobot.isAutoPickupEnabled();
                boolean tempDisabled = !neurorobot.isPickupAllowed();

                String status;
                if (tempDisabled) {
                    status = "Auto pickup is temporarily disabled";
                } else if (autoEnabled) {
                    status = "Auto pickup is enabled";
                } else {
                    status = "Auto pickup is disabled";
                }

                player.getServer().getPlayerManager().broadcast(
                        createNeurorobotMessage(status),
                        false
                );
            }
            else if (commandText.contains("use tool") || commandText.contains("使用工具")) {
                // 手动切换工具命令
                player.getServer().getPlayerManager().broadcast(
                        createNeurorobotMessage("I will automatically select the best tool for mining."),
                        false
                );
            }
            else if (commandText.contains("what can you do") || commandText.contains("你能做什么")) {
                player.getServer().getPlayerManager().broadcast(
                        createNeurorobotMessage("I can follow you, mine blocks, attack monsters, pick up items, manage inventory, use tools, and more! Try 'inventory', 'drop', 'pickup', 'mine', or 'auto pickup'."),
                        false
                );
            }
            else if (commandText.contains("thank") || commandText.contains("谢谢")) {
                player.getServer().getPlayerManager().broadcast(
                        createNeurorobotMessage("You're welcome!"),
                        false
                );
            }
            else if (commandText.contains("how are you") || commandText.contains("你好吗")) {
                player.getServer().getPlayerManager().broadcast(
                        createNeurorobotMessage("I'm functioning optimally. Ready to assist!"),
                        false
                );
            }
            // 在 ChatListener.java 中添加工具状态命令
            else if (commandText.contains("tool status") || commandText.contains("工具状态")) {
                StringBuilder toolInfo = new StringBuilder("Tool Status:\n");
                boolean hasTools = false;

                for (int i = 0; i < neurorobot.getInventory().size(); i++) {
                    ItemStack stack = neurorobot.getInventory().getStack(i);
                    if (!stack.isEmpty() && stack.getMaxDamage() > 0) {
                        hasTools = true;
                        int damage = stack.getDamage();
                        int maxDamage = stack.getMaxDamage();
                        int remaining = maxDamage - damage;
                        float percentage = (float) remaining / maxDamage * 100;

                        toolInfo.append(stack.getName().getString())
                                .append(": ").append(remaining).append("/").append(maxDamage)
                                .append(" (").append(String.format("%.1f", percentage)).append("%)\n");
                    }
                }

                if (!hasTools) {
                    toolInfo.append("No tools in inventory");
                }

                player.getServer().getPlayerManager().broadcast(
                        createNeurorobotMessage(toolInfo.toString()),
                        false
                );
            }
            else if (commandText.contains("debug") || commandText.contains("调试")) {
                neurorobot.debugStatus();
            }
            else{
                player.getServer().getPlayerManager().broadcast(
                        createNeurorobotMessage("I don't understand that command. Available commands: 'follow', 'stop', 'mine', 'attack', 'inventory', 'drop', 'drop all', 'pickup', 'auto pickup', 'no pickup', 'enable pickup', 'pickup status', 'tool status', 'debug'. Say 'what can you do' for more info."),
                        false
                );
            }
        });
    }

    // 创建带有橙色加粗前缀的Neurorobot消息
    private static Text createNeurorobotMessage(String message) {
        MutableText prefix = Text.literal("[Neurorobot] ")
                .setStyle(Style.EMPTY.withColor(Formatting.GOLD).withBold(true));
        MutableText content = Text.literal(message)
                .setStyle(Style.EMPTY.withColor(Formatting.WHITE).withBold(false));

        return prefix.append(content);
    }
}
