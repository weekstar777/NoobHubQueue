package com.noobhub.queue.listeners;

import com.noobhub.queue.NoobHubQueue;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;

/**
 * 访客模式监听器
 * 限制在等待平台的玩家只能移动和空A
 */
public class GuestModeListener implements Listener {

    private final NoobHubQueue plugin;

    public GuestModeListener(NoobHubQueue plugin) {
        this.plugin = plugin;
    }

    /**
     * 检查玩家是否在访客模式（在排队中）
     */
    private boolean isGuest(Player player) {
        return plugin.getQueueManager().isInQueue(player.getUniqueId());
    }

    /**
     * 阻止访客与方块/物品交互
     */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (isGuest(event.getPlayer())) {
            event.setCancelled(true);
        }
    }

    /**
     * 阻止访客破坏方块
     */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        if (isGuest(event.getPlayer())) {
            event.setCancelled(true);
        }
    }

    /**
     * 阻止访客放置方块
     */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent event) {
        if (isGuest(event.getPlayer())) {
            event.setCancelled(true);
        }
    }

    /**
     * 阻止访客丢弃物品
     */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        if (isGuest(event.getPlayer())) {
            event.setCancelled(true);
        }
    }

    /**
     * 阻止访客拾取物品
     */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerPickupItem(PlayerPickupItemEvent event) {
        if (isGuest(event.getPlayer())) {
            event.setCancelled(true);
        }
    }

    /**
     * 阻止访客使用背包
     */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getWhoClicked() instanceof Player) {
            Player player = (Player) event.getWhoClicked();
            if (isGuest(player)) {
                event.setCancelled(true);
            }
        }
    }

    /**
     * 阻止访客攻击实体（除了空手）
     * 空A是允许的
     */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player) {
            Player player = (Player) event.getDamager();
            if (isGuest(player)) {
                // 检查是否是空手
                if (player.getInventory().getItemInMainHand().getType().isAir()) {
                    // 允许空A
                } else {
                    // 有物品时不允许攻击
                    event.setCancelled(true);
                }
            }
        }
    }

    /**
     * 阻止访客饥饿值变化
     */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onFoodLevelChange(FoodLevelChangeEvent event) {
        if (event.getEntity() instanceof Player) {
            Player player = (Player) event.getEntity();
            if (isGuest(player)) {
                event.setCancelled(true);
            }
        }
    }
}
