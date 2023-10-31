package com.conaxgames.discordsync.bukkit.task;

import com.conaxgames.discordsync.bukkit.DiscordSyncPlugin;
import com.conaxgames.discordsync.bukkit.MessageConfig;
import com.conaxgames.discordsync.bukkit.profile.Profile;
import lombok.RequiredArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.scheduler.BukkitRunnable;

@RequiredArgsConstructor
public class AutoBroadcastTask extends BukkitRunnable {
    private final DiscordSyncPlugin plugin;

    public void run() {
        Bukkit.getOnlinePlayers().stream().filter(player -> {
            Profile profile = plugin.getProfileManager().getProfile(player);
            return profile != null && (profile.getCode() == null || !profile.isUsedCode());
        }).forEach(player -> MessageConfig.NOT_SYNCED_BROADCAST
                .forEach(message -> player
                        .sendMessage(ChatColor.translateAlternateColorCodes(
                                '&', message.replace("%PLAYER%", player.getName())))));
    }
}
