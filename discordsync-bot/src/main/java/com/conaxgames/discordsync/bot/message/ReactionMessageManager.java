package com.conaxgames.discordsync.bot.message;

import com.conaxgames.discordsync.bot.DiscordSyncBot;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

public class ReactionMessageManager {
    private final DiscordSyncBot bot;
    @Getter @Setter private List<RoleReactMessage> messages;

    public ReactionMessageManager(DiscordSyncBot bot) {
        this.bot = bot;
    }

    public void addMessage(RoleReactMessage message) {
        this.messages.add(message);
        this.bot.getSqlManager().addMessage(message);
    }
}
