package com.noobhub.queue.utils;

import org.bukkit.entity.Player;

public class MessageUtil {

    public static void send(Player player, String message) {
        if (message == null || message.isEmpty()) {
            return;
        }
        player.sendMessage(message.replace("&", "§"));
    }
}
