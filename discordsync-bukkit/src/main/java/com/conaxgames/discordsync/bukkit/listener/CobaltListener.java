package com.conaxgames.discordsync.bukkit.listener;

import com.conaxgames.discordsync.bukkit.DiscordSyncPlugin;
import com.conaxgames.event.player.MinemanRetrieveEvent;
import com.conaxgames.event.player.RankChangeEvent;
import lombok.RequiredArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

@RequiredArgsConstructor
public class CobaltListener implements Listener {
    private final DiscordSyncPlugin plugin;

    @EventHandler
    public void onRankChange(RankChangeEvent event) {
        Bukkit.getScheduler().runTaskLaterAsynchronously(plugin,
                () -> plugin.getRedisManager().updatePlayer(event.getUuid(), event.getTo().getName()), 1L);
    }
}
