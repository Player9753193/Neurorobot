package com.neurorobot.listener;

import net.fabricmc.fabric.api.message.v1.ServerMessageEvents;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import com.neurorobot.Neurorobot;

public class NeurorobotChatListener {

    public static void register() {
        ServerMessageEvents.CHAT_MESSAGE.register((message, player, params) -> {
            String rawMessage = message.getContent().getString();

            // 检查世界中的所有神经机器人并传递消息
            player.getServerWorld().getEntitiesByType(Neurorobot.NEUROROBOT, entity -> true).forEach(neurorobot -> {
                // 检查神经机器人是否在玩家附近（32格内）
                if (neurorobot.squaredDistanceTo(player) <= 1024) { // 32^2
                    neurorobot.processPlayerMessage(player, rawMessage);
                }
            });
        });
    }
}