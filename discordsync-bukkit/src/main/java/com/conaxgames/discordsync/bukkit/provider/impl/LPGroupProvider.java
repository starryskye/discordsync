package com.conaxgames.discordsync.bukkit.provider.impl;

import com.conaxgames.discordsync.bukkit.provider.IGroupProvider;
import me.lucko.luckperms.LuckPerms;
import me.lucko.luckperms.api.LuckPermsApi;

import java.util.Objects;
import java.util.UUID;

public class LPGroupProvider implements IGroupProvider {
    private final LuckPermsApi api;

    public LPGroupProvider() {
        this.api = LuckPerms.getApi();
    }

    public String getGroup(UUID uuid) {
        if (api.isUserLoaded(uuid)) {
            return Objects.requireNonNull(api.getUserManager().getUser(uuid)).getPrimaryGroup();
        }

        return null;
    }
}
