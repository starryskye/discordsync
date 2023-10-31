package com.conaxgames.discordsync.bukkit.provider.impl;

import com.conaxgames.CorePlugin;
import com.conaxgames.discordsync.bukkit.provider.IGroupProvider;
import org.bukkit.Bukkit;

import java.util.UUID;

public class CobaltGroupProvider implements IGroupProvider {
    private final CorePlugin plugin;

    public CobaltGroupProvider() {
        this.plugin = (CorePlugin) Bukkit.getPluginManager().getPlugin("Cobalt");
    }

    public String getGroup(UUID uuid) {
        return plugin.getPlayerManager().getPlayer(uuid).getRank().getName();
    }
}
