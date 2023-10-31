package com.conaxgames.discordsync.bukkit.listener;

import com.conaxgames.discordsync.bukkit.DiscordSyncPlugin;
import me.lucko.luckperms.LuckPerms;
import me.lucko.luckperms.api.LuckPermsApi;
import me.lucko.luckperms.api.User;
import me.lucko.luckperms.api.event.user.UserDataRecalculateEvent;

public class LuckPermsListener {
    private final DiscordSyncPlugin plugin;
    private final LuckPermsApi api;

    public LuckPermsListener(DiscordSyncPlugin plugin) {
        this.plugin = plugin;
        
        this.api = LuckPerms.getApi();
        this.api.getEventBus().subscribe(UserDataRecalculateEvent.class, this::onUserDataRecalculateEvent);
    }

    private void onUserDataRecalculateEvent(UserDataRecalculateEvent event) {
        User user = event.getUser();
        plugin.getRedisManager().updatePlayer(user.getUuid(), user.getPrimaryGroup());
    }
}
