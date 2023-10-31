package com.conaxgames.discordsync.bukkit;

import org.bukkit.configuration.file.FileConfiguration;

import java.util.List;

public class MessageConfig {
    public static final List<String> NOT_SYNCED_BROADCAST;
    public static final List<String> UNUSED_CODE;
    public static final List<String> ALREADY_LINKED;
    public static final List<String> CREATED_CODE;

    static {
        FileConfiguration config = DiscordSyncPlugin.getInstance().getMessageConfig();
        NOT_SYNCED_BROADCAST = config.getStringList("not-synced-broadcast");
        UNUSED_CODE = config.getStringList("unused-code");
        ALREADY_LINKED = config.getStringList("already-linked");
        CREATED_CODE = config.getStringList("created-code");
    }
}
