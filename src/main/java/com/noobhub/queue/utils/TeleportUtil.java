package com.noobhub.queue.utils;

import com.noobhub.queue.NoobHubQueue;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class TeleportUtil {

    public static void teleportToLobby(Player player, NoobHubQueue plugin) {
        Location lobby = plugin.getConfigManager().getLobbyLocation();
        if (lobby != null && lobby.getWorld() != null) {
            player.teleport(lobby);
            plugin.getLogger().info("传送 " + player.getName() + " 到等待平台");
        } else {
            plugin.getLogger().warning("无法传送 " + player.getName() + "：等待平台位置无效");
        }
    }
}
