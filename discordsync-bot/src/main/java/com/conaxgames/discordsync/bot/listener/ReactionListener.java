package com.conaxgames.discordsync.bot.listener;

import com.conaxgames.discordsync.bot.DiscordSyncBot;
import com.conaxgames.discordsync.bot.message.RoleReactMessage;
import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.core.events.message.guild.react.GuildMessageReactionAddEvent;
import net.dv8tion.jda.core.events.message.guild.react.GuildMessageReactionRemoveEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;

@RequiredArgsConstructor
public class ReactionListener extends ListenerAdapter {
    private final DiscordSyncBot bot;

    public void onGuildMessageReactionAdd(GuildMessageReactionAddEvent event) {
        for (RoleReactMessage message : bot.getReactionManager().getMessages()) {
            if (event.getMessageId().equals(message.getMessageId())) {
                bot.getGuild().getController().addRolesToMember(
                        bot.getGuild().getMember(event.getUser()),
                        message.getRole()
                ).queue();
                break;
            }
        }
    }

    public void onGuildMessageReactionRemove(GuildMessageReactionRemoveEvent event) {
        for (RoleReactMessage message : bot.getReactionManager().getMessages()) {
            if (event.getMessageId().equals(message.getMessageId())) {
                bot.getGuild().getController().removeSingleRoleFromMember(
                        bot.getGuild().getMember(event.getUser()),
                        message.getRole()
                ).queue();
                break;
            }
        }
    }
}
