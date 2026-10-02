package com.noobhub.queue.listeners;

import com.noobhub.queue.NoobHubQueue;
import com.noobhub.queue.utils.MessageUtil;
import com.onarandombox.MultiverseCore.MultiverseCore;
import com.onarandombox.MultiverseCore.api.MVWorldManager;
import com.onarandombox.MultiverseCore.api.MultiverseWorld;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.Plugin;

public class PlayerJoinListener implements Listener {

    private final NoobHubQueue plugin;

    public PlayerJoinListener(NoobHubQueue plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        // 使用Multiverse API获取spawn点（而不是Bukkit的getSpawnLocation）
        Plugin mvPlugin = Bukkit.getPluginManager().getPlugin("Multiverse-Core");
        if (mvPlugin instanceof MultiverseCore) {
            MultiverseCore mvCore = (MultiverseCore) mvPlugin;
            MVWorldManager worldManager = mvCore.getMVWorldManager();
            MultiverseWorld mvWorld = worldManager.getMVWorld("world_lobby");

            if (mvWorld != null) {
                Location spawnLocation = mvWorld.getSpawnLocation();
                player.teleport(spawnLocation);
                player.setGameMode(GameMode.ADVENTURE);
                plugin.getLogger().info(player.getName() + " 传送到主城spawn (Multiverse): " +
                    spawnLocation.getX() + ", " + spawnLocation.getY() + ", " + spawnLocation.getZ());
            }
        }

        plugin.getLogger().info(player.getName() + " 进入服务器");

        // 发送欢迎消息和操作提示
        MessageUtil.send(player, "§a§l欢迎来到 NoobHub！");
        MessageUtil.send(player, "§7输入 §6/play §7进入游戏");
    }
}
