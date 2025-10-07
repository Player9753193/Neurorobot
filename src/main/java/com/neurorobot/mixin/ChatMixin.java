package com.neurorobot.mixin;

import com.neurorobot.entity.NeurorobotEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(MinecraftServer.class)
public class ChatMixin {
    @Inject(at = @At("HEAD"), method = "sendMessage")
    private void onChatMessage(Text message, CallbackInfo info) {
        MinecraftServer server = (MinecraftServer) (Object) this;
        List<ServerPlayerEntity> players = server.getPlayerManager().getPlayerList();

        for (ServerPlayerEntity player : players) {
            String text = message.getString().toLowerCase();

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
                continue;
            }

            // 查找10格内的Neurorobot
            List<NeurorobotEntity> neurorobots = player.getServerWorld().getEntitiesByClass(
                    NeurorobotEntity.class,
                    player.getBoundingBox().expand(32.0),
                    entity -> true
            );

            if (neurorobots.isEmpty()) {
                player.sendMessage(createNeurorobotMessage("I'm not nearby. Please summon me first."), false);
                continue;
            }

            NeurorobotEntity neurorobot = neurorobots.get(0);

            // 处理各种命令
            if (commandText.isEmpty() || commandText.contains("hello") || commandText.contains("hi") || commandText.contains("你好")) {
                server.getPlayerManager().broadcast(
                        createNeurorobotMessage("Hello, " + player.getName().getString() + "! How can I help you?"),
                        false
                );
            }
            else if (commandText.contains("follow") || commandText.contains("跟随")) {
                neurorobot.startFollowing(player);
                server.getPlayerManager().broadcast(
                        createNeurorobotMessage("I will follow you, " + player.getName().getString() + "."),
                        false
                );
            }
            else if (commandText.contains("stop") || commandText.contains("停止")) {
                neurorobot.stopAllActions();
                server.getPlayerManager().broadcast(
                        createNeurorobotMessage("I've stopped all actions."),
                        false
                );
            }
            else if (commandText.contains("mine") || commandText.contains("挖掘")) {
                neurorobot.startMining();
                server.getPlayerManager().broadcast(
                        createNeurorobotMessage("I will start mining nearby blocks."),
                        false
                );
            }
            else if (commandText.contains("attack") || commandText.contains("攻击")) {
                neurorobot.startAttacking();
                server.getPlayerManager().broadcast(
                        createNeurorobotMessage("I will attack nearby monsters."),
                        false
                );
            }
            else if (commandText.contains("what can you do") || commandText.contains("你能做什么")) {
                server.getPlayerManager().broadcast(
                        createNeurorobotMessage("I can follow you, mine blocks, attack monsters, and more! Just ask."),
                        false
                );
            }
            else if (commandText.contains("thank") || commandText.contains("谢谢")) {
                server.getPlayerManager().broadcast(
                        createNeurorobotMessage("You're welcome!"),
                        false
                );
            }
            else if (commandText.contains("how are you") || commandText.contains("你好吗")) {
                server.getPlayerManager().broadcast(
                        createNeurorobotMessage("I'm functioning optimally. Ready to assist!"),
                        false
                );
            }
            else if (commandText.contains("stay") || commandText.contains("等待")) {
                //neurorobot.stay();
                server.getPlayerManager().broadcast(
                        createNeurorobotMessage("I will stay here."),
                        false
                );
            }
            else {
                // 对于无法识别的命令，给出友好回应
                server.getPlayerManager().broadcast(
                        createNeurorobotMessage("I don't understand that command. Try 'follow', 'mine', 'attack', or 'stop'."),
                        false
                );
            }
        }
    }

    // 创建带有橙色加粗前缀的Neurorobot消息
    private Text createNeurorobotMessage(String message) {
        MutableText prefix = Text.literal("[Neurorobot] ")
                .setStyle(Style.EMPTY.withColor(Formatting.GOLD).withBold(true));
        MutableText content = Text.literal(message)
                .setStyle(Style.EMPTY.withColor(Formatting.WHITE).withBold(false));

        return prefix.append(content);
    }
}
