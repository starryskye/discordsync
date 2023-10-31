package com.conaxgames.discordsync.bot.command.impl;

import com.conaxgames.discordsync.bot.DiscordSyncBot;
import com.conaxgames.discordsync.bot.command.ICommand;
import com.conaxgames.discordsync.bot.util.RoleUtil;
import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Role;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;

import java.awt.*;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
public class RoleCommand implements ICommand {
    private final DiscordSyncBot bot;

    public boolean handle(User user, TextChannel channel, String args) {
        Member member = bot.getGuild().getMember(user);
        if (!bot.getSqlManager().isLinked(user)) {
            EmbedBuilder builder = new EmbedBuilder()
                    .addField("Not linked.", "Your discord is not linked to any account.", false)
                    .setColor(Color.RED);
            user.openPrivateChannel().queue(privateChannel ->
                    privateChannel.sendMessage(builder.build()).queue());
            return true;
        }

        String query = "SELECT rank, username FROM users WHERE discord_id=?";
        try (PreparedStatement statement = bot.getSqlManager().getConnection().prepareStatement(query)) {
            statement.setString(1, user.getId());
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    String rank = resultSet.getString("rank");
                    List<Role> rankRoles = bot.getRankRoles().get(rank);;
                    List<Role> remove = RoleUtil.getRemoveList(member, rankRoles);

                    String prefix = rank.equals("Normal") ? "" : "[" + rank.replace("-", " ") + "] ";
                    bot.getGuild().getController().setNickname(member, prefix + resultSet.getString("username")).queue();
                    bot.getGuild().getController().modifyMemberRoles(member, rankRoles, remove).queue();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return true;
    }
}
