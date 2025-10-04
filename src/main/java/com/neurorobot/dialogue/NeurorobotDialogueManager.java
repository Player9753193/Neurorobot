package com.neurorobot.dialogue;

import com.neurorobot.entity.NeurorobotEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.*;
import java.util.regex.Pattern;

public class NeurorobotDialogueManager {
    private final NeurorobotEntity neurorobot;
    private PlayerEntity currentSpeaker;
    private long lastInteractionTime;

    // 记忆系统 - 记住最近的对话和指令
    private final List<String> conversationHistory = new ArrayList<>();
    private static final int MAX_HISTORY = 10;

    public NeurorobotDialogueManager(NeurorobotEntity neurorobot) {
        this.neurorobot = neurorobot;
        this.lastInteractionTime = neurorobot.getWorld().getTime();
    }

    public void processMessage(PlayerEntity player, String message) {
        this.currentSpeaker = player;
        this.lastInteractionTime = neurorobot.getWorld().getTime();

        // 添加到对话历史
        addToHistory("Player: " + message);

        // 标准化消息（小写，去除多余空格）
        String normalizedMessage = message.toLowerCase().trim();

        // 检查是否在呼叫神经机器人
        if (isCallingNeurorobot(normalizedMessage)) {
            String response = generateResponse(normalizedMessage);
            neurorobot.sendMessageToPlayer(player, response);
            executeAction(normalizedMessage, player);
            addToHistory("Neurorobot: " + response);
        }
    }

    private boolean isCallingNeurorobot(String message) {
        // 检查消息是否包含对神经机器人的呼叫
        return message.contains("neurorobot") ||
                message.contains("robot") ||
                message.contains("assistant") ||
                message.startsWith("nr ") ||
                message.contains("hey") && (message.contains("bot") || message.contains("robot"));
    }

    private String generateResponse(String message) {
        // 基于关键词的响应生成
        Random random = new Random();

        if (containsAny(message, "hello", "hi", "hey", "greetings")) {
            String[] greetings = {
                    "Hello! How can I assist you today?",
                    "Hi there! What can I do for you?",
                    "Greetings! I'm ready to help.",
                    "Hello! I'm listening."
            };
            return greetings[random.nextInt(greetings.length)];
        }

        if (containsAny(message, "follow", "come", "with me")) {
            String[] responses = {
                    "I'll follow you closely!",
                    "Okay, I'll stick with you!",
                    "Following you now!",
                    "I'm right behind you!",
                    "Let's go together!"
            };
            return responses[random.nextInt(responses.length)];
        }

        if (containsAny(message, "stay", "stop", "wait", "remain")) {
            String[] responses = {
                    "I'll stay here.",
                    "Okay, I'll wait.",
                    "Staying put.",
                    "I'll remain here as requested."
            };
            return responses[random.nextInt(responses.length)];
        }

        if (containsAny(message, "mine", "dig", "gather", "collect")) {
            if (containsAny(message, "gold", "ore")) {
                return "I'll search for gold ore and mine it for you!";
            }
            if (containsAny(message, "iron")) {
                return "I'll look for iron ore to mine.";
            }
            if (containsAny(message, "diamond")) {
                return "I'll try to find some diamonds for you!";
            }
            if (containsAny(message, "stone", "cobblestone")) {
                return "I'll gather some stone for you.";
            }
            return "What would you like me to mine?";
        }

        if (containsAny(message, "bread", "food", "hungry", "eat")) {
            return "I don't have any bread, but I can help you find food!";
        }

        if (containsAny(message, "how are you", "how do you feel")) {
            String[] responses = {
                    "I'm functioning optimally!",
                    "All systems operational!",
                    "I'm doing well, thank you for asking!",
                    "I'm ready to assist!"
            };
            return responses[random.nextInt(responses.length)];
        }

        if (containsAny(message, "thank", "thanks")) {
            String[] responses = {
                    "You're welcome!",
                    "Happy to help!",
                    "Anytime!",
                    "Glad I could assist!"
            };
            return responses[random.nextInt(responses.length)];
        }

        if (containsAny(message, "what can you do", "help", "abilities")) {
            return "I can follow you, stay in place, mine resources, and have simple conversations! Just ask me to follow, stay, or mine something.";
        }

        // 默认响应
        String[] defaultResponses = {
                "I'm not sure I understand. Can you rephrase that?",
                "Could you clarify what you'd like me to do?",
                "I didn't catch that. Try asking me to follow, stay, or mine something.",
                "I'm still learning! Try simpler commands like 'follow me' or 'stay here'."
        };
        return defaultResponses[random.nextInt(defaultResponses.length)];
    }

    // 在 executeAction 方法中添加 Baritone 调用
    // 在 executeAction 方法中确保使用正确的 Baritone 调用
    private void executeAction(String message, PlayerEntity player) {
        World world = neurorobot.getWorld();

        if (containsAny(message, "follow", "come", "with me","跟着我")) {
            neurorobot.followPlayer(player);
        }

        if (containsAny(message, "stop", "cancel", "abort", "stay", "wait", "remain","停下","待在这里")) {
            neurorobot.stopAllTasks();
        }

        if (containsAny(message, "mine", "dig", "gather","收集","采集","挖")) {
            // 停止跟随以进行挖掘
            neurorobot.stopAllTasks();

            if (containsAny(message, "gold")) {
                neurorobot.mineResources("gold");
            } else if (containsAny(message, "iron")) {
                neurorobot.mineResources("iron");
            } else if (containsAny(message, "diamond")) {
                neurorobot.mineResources("diamond");
            } else if (containsAny(message, "coal")) {
                neurorobot.mineResources("coal");
            } else if (containsAny(message, "redstone")) {
                neurorobot.mineResources("redstone");
            } else if (containsAny(message, "lapis")) {
                neurorobot.mineResources("lapis");
            } else if (containsAny(message, "emerald")) {
                neurorobot.mineResources("emerald");
            } else if (containsAny(message, "copper")) {
                neurorobot.mineResources("copper");
            } else if (containsAny(message, "stone")) {
                neurorobot.mineResources("stone");
            } else {
                neurorobot.mineResources("resources");
            }
        }

        if (containsAny(message, "explore", "scout", "survey")) {
            // 停止跟随以进行探索
            neurorobot.stopAllTasks();
            int radius = 50;
            neurorobot.exploreArea(radius);
        }

        if (containsAny(message, "goto", "go to") && containsAny(message, "position", "coord", "coordinate")) {
            // 停止跟随以移动到指定位置
            neurorobot.stopAllTasks();
            // 这里可以添加坐标解析逻辑
            neurorobot.sendMessageToPlayer(player, "Goto command detected. This feature requires coordinate parsing.");
        }

        if (containsAny(message, "status", "report", "how are you")) {
            neurorobot.sendMessageToPlayer(player, neurorobot.getStatusReport());
        }
    }

    private boolean containsAny(String message, String... keywords) {
        for (String keyword : keywords) {
            if (message.contains(keyword)) {
                return true;
            }
        }
        return false;
    }

    private void addToHistory(String entry) {
        conversationHistory.add(entry);
        if (conversationHistory.size() > MAX_HISTORY) {
            conversationHistory.remove(0);
        }
    }

    public List<String> getConversationHistory() {
        return new ArrayList<>(conversationHistory);
    }

    public boolean isInConversation() {
        return neurorobot.getWorld().getTime() - lastInteractionTime < 200; // 10秒内算在对话中
    }
}