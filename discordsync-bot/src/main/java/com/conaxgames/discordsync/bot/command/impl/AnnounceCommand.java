package com.conaxgames.discordsync.bot.command.impl;

import com.conaxgames.discordsync.bot.DiscordSyncBot;
import com.conaxgames.discordsync.bot.command.ICommand;
import com.conaxgames.discordsync.bot.util.RoleUtil;
import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;

import java.time.Instant;

@RequiredArgsConstructor
public class AnnounceCommand implements ICommand {
    private final DiscordSyncBot bot;

    public boolean handle(User user, TextChannel channel, String args) {
        Member member = bot.getGuild().getMember(user);
        if (!RoleUtil.hasRole(member, "Manager", "Developer", "Owner")) {
            return false;
        }

        String message = bot.getGuild().getPublicRole().getAsMention() + " " + args;

        EmbedBuilder builder = new EmbedBuilder()
                .setTitle("Announcement")
                .setDescription(message)
                .setTimestamp(Instant.now())
                .setFooter(member.getNickname(), user.getAvatarUrl());

        channel.sendMessage(builder.build()).queue();
        return true;
    }
}
