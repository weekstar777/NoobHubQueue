package com.noobhub.queue;

import com.noobhub.queue.commands.PlayCommand;
import com.noobhub.queue.config.ConfigManager;
import com.noobhub.queue.database.DatabaseManager;
import com.noobhub.queue.database.PlayerDataRepository;
import com.noobhub.queue.listeners.PlayerJoinListener;
import com.noobhub.queue.listeners.PlayerQuitListener;
import com.noobhub.queue.listeners.PlayerRespawnListener;
import com.noobhub.queue.managers.QueueManager;
import org.bukkit.plugin.java.JavaPlugin;

public final class NoobHubQueue extends JavaPlugin {

    private static NoobHubQueue instance;
    private QueueManager queueManager;
    private ConfigManager configManager;
    private DatabaseManager databaseManager;
    private PlayerDataRepository playerDataRepository;

    @Override
    public void onEnable() {
        instance = this;

        // 加载配置
        saveDefaultConfig();
        configManager = new ConfigManager(this);

        // 初始化数据库
        databaseManager = new DatabaseManager(this);
        playerDataRepository = new PlayerDataRepository(databaseManager);

        // 初始化队列管理器
        queueManager = new QueueManager(this);

        // 注册监听器
        getServer().getPluginManager().registerEvents(new PlayerJoinListener(this), this);
        getServer().getPluginManager().registerEvents(new PlayerQuitListener(this), this);
        getServer().getPluginManager().registerEvents(new PlayerRespawnListener(this), this);

        // 注册指令
        getCommand("play").setExecutor(new PlayCommand(this));

        // 启动定时检查任务
        queueManager.startQueueCheck();

        getLogger().info("NoobHubQueue 已启用！");
        getLogger().info("最大游戏人数: " + configManager.getMaxGamePlayers());
        getLogger().info("MySQL 数据库已连接");
        getLogger().info("已注册 /play 指令");
    }

    @Override
    public void onDisable() {
        // 停止定时任务
        if (queueManager != null) {
            queueManager.stopQueueCheck();
        }

        // 关闭数据库连接
        if (databaseManager != null) {
            databaseManager.close();
        }

        getLogger().info("NoobHubQueue 已禁用！");
    }

    public static NoobHubQueue getInstance() {
        return instance;
    }

    public QueueManager getQueueManager() {
        return queueManager;
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public DatabaseManager getDatabaseManager() {
        return databaseManager;
    }

    public PlayerDataRepository getPlayerDataRepository() {
        return playerDataRepository;
    }
}
