package com.conaxgames.discordsync.bukkit.listener;

import com.conaxgames.discordsync.bukkit.DiscordSyncPlugin;
import com.conaxgames.discordsync.bukkit.profile.Profile;
import lombok.RequiredArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.concurrent.ExecutionException;

@RequiredArgsConstructor
public class ProfileListeners implements Listener {
    private final DiscordSyncPlugin plugin;

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            Profile profile;
            try {
                profile = plugin.getProfileManager().createPlayer(event.getPlayer()).load().get();
            } catch (Exception e) {
                e.printStackTrace();
                return;
            }

            if (!profile.getName().equals(player.getName())) {
                plugin.getRedisManager().updateName(player.getUniqueId(), player.getName());
                profile.setName(player.getName());
            }
        });
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        plugin.getProfileManager().removePlayer(event.getPlayer());
    }
}
