package com.conaxgames.discordsync.bukkit;

import com.conaxgames.discordsync.bukkit.command.LinkCommand;
import com.conaxgames.discordsync.bukkit.database.SQLManager;
import com.conaxgames.discordsync.bukkit.database.redis.PluginSubscriber;
import com.conaxgames.discordsync.bukkit.listener.ProfileListeners;
import com.conaxgames.discordsync.bukkit.profile.ProfileManager;
import com.conaxgames.discordsync.bukkit.provider.IGroupProvider;
import com.conaxgames.discordsync.bukkit.provider.impl.CobaltGroupProvider;
import com.conaxgames.discordsync.bukkit.database.redis.RedisManager;
import com.conaxgames.discordsync.bukkit.listener.CobaltListener;
import com.conaxgames.discordsync.bukkit.listener.LuckPermsListener;
import com.conaxgames.discordsync.bukkit.provider.impl.LPGroupProvider;
import com.conaxgames.discordsync.bukkit.task.AutoBroadcastTask;
import lombok.Getter;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.sql.SQLException;

@Getter
public class DiscordSyncPlugin extends JavaPlugin {
    @Getter private static DiscordSyncPlugin instance;

    private IGroupProvider groupProvider;
    private YamlConfiguration messageConfig;
    private SQLManager sqlManager;
    private RedisManager redisManager;
    private ProfileManager profileManager;

    public void onEnable() {
        DiscordSyncPlugin.instance = this;
        loadConfigs();

        PluginManager pm = getServer().getPluginManager();
        pm.registerEvents(new ProfileListeners(this), this);
        if (pm.getPlugin("Cobalt") != null) {
            this.groupProvider = new CobaltGroupProvider();
            pm.registerEvents(new CobaltListener(this), this);
        } else if (pm.getPlugin("LuckPerms") != null) {
            this.groupProvider = new LPGroupProvider();
            new LuckPermsListener(this);
        } else {
            getLogger().severe("LuckPerms and Cobalt not found, disabling.");
            pm.disablePlugin(this);
            return;
        }

        this.sqlManager = new SQLManager(this);
        this.redisManager = new RedisManager(this);
        this.profileManager = new ProfileManager();

        new PluginSubscriber(this);

        getCommand("link").setExecutor(new LinkCommand(this));
        new AutoBroadcastTask(this).runTaskTimer(this, 20 * 60 * 5, 20 * 60 * 5);
    }

    public void onDisable() {
        try {
            this.sqlManager.getConnection().close();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        DiscordSyncPlugin.instance = null;
    }

    private void loadConfigs() {
        this.saveDefaultConfig();

        File messageConfigFile = new File(this.getDataFolder(), "messages.yml");
        if (!messageConfigFile.exists()) {
            this.saveResource("messages.yml", false);
        }

        this.messageConfig = YamlConfiguration.loadConfiguration(messageConfigFile);
    }
}