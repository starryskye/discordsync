package com.conaxgames.discordsync.bot.listener;

import com.conaxgames.discordsync.bot.DiscordSyncBot;
import com.moandjiezana.toml.Toml;
import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageHistory;
import net.dv8tion.jda.core.entities.Role;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.events.ReadyEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

@RequiredArgsConstructor
public class ReadyListener extends ListenerAdapter {
    private final DiscordSyncBot bot;

    public void onReady(ReadyEvent event) {
        bot.setGuild(bot.getJda().getGuildById(bot.getConfig().getLong("discord.server")));
        bot.setVerifiedRole(bot.getGuild().getRolesByName("Verified", true).get(0));

        bot.setUhcRole(bot.getGuild().getRolesByName("UHC", true).get(0));
        bot.setUhcFeedChannel(bot.getGuild().getTextChannelsByName("uhc-feed", false).get(0));

        bot.setPlayerRole(bot.getGuild().getRolesByName("Player", true).get(0));

        bot.getReactionManager().setMessages(bot.getSqlManager().getMessages());

        TextChannel rankSyncChannel = bot.getGuild().getTextChannelsByName("rank-sync", false).get(0);

        AtomicBoolean deleting = new AtomicBoolean(true);
        MessageHistory history = new MessageHistory(rankSyncChannel);
        new Thread(() -> {
            while (deleting.get()) {
                List<Message> messages = history.retrievePast(50).complete();
                if (messages.isEmpty()) {
                    deleting.set(false);
                    return;
                }

                rankSyncChannel.deleteMessages(messages).complete();
            }
        }).run();

        Toml rankTable = bot.getConfig().getTable("ranks");
        if (rankTable != null) {
            for (Map.Entry<String, Object> entry : rankTable.entrySet()) {
                if (entry.getValue() instanceof String) {
                    String name = (String) entry.getValue();
                    List<Role> roles = bot.getGuild().getRolesByName(name, true);
                    roles.add(bot.getVerifiedRole());
                    roles.add(bot.getPlayerRole());

                    bot.getRankRoles().put(entry.getKey().replace("\"", ""), roles);
                } else if (entry.getValue() instanceof List) {
                    List<Role> roles = new ArrayList<>();
                    roles.add(bot.getVerifiedRole());
                    roles.add(bot.getPlayerRole());
                    ((List<String>) entry.getValue()).forEach(r -> {
                        roles.add(bot.getGuild().getRolesByName(r, true).get(0)); // this is really bad wtf toml is on some shit
                    });

                    bot.getRankRoles().put(entry.getKey().replace("\"", ""), roles);
                } else {
                    throw new IllegalStateException("" + entry.getValue().getClass());
                }
            }
        }
    }
}
