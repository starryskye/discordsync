package com.conaxgames.discordsync.bot.listener;

import com.conaxgames.discordsync.bot.DiscordSyncBot;
import com.conaxgames.discordsync.bot.command.ICommand;
import com.conaxgames.discordsync.bot.util.RoleUtil;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.*;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;

import java.awt.*;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class MessageListener extends ListenerAdapter {
    private final DiscordSyncBot bot;
    private String channel;

    public MessageListener(DiscordSyncBot bot) {
        this.bot = bot;
        this.channel = this.bot.getConfig().getString("discord.channel");
    }

    public void onMessageReceived(MessageReceivedEvent event) {
        if (!event.isFromType(ChannelType.TEXT)) {
            return;
        }

        String message = event.getMessage().getContentRaw();
        if (message.startsWith("!")) {
            String[] split = message.split(" ");
            String command = split[0].replace("!", "").toLowerCase();
            ICommand cmd = bot.getCommandMap().get(command);

            if (cmd != null) {
                StringBuilder args = new StringBuilder();
                for (int i = 1; i < split.length; i++) {
                    args.append(split[i] + " ");
                }

                if (cmd.handle(event.getAuthor(), event.getTextChannel(), args.toString().trim())) {
                    event.getMessage().delete().queue();
                }

                return;
            }
        }

        if (!event.getTextChannel().getName().equalsIgnoreCase(this.channel)) {
            return;
        }

        if (event.getAuthor().isBot()) {
            return;
        }

        event.getMessage().delete().queue();

        if (message.length() != 8) {
            return;
        }

        Guild guild = event.getGuild();
        Member member = event.getMember();
        User user = event.getAuthor();

        guild.getController().removeRolesFromMember(member, member.getRoles()).queue();
        /*List<Role> roles = bot.getRankRoles().get("Normal");
        guild.getController().addRolesToMember(member, roles).queue();*/

        if (bot.getSqlManager().isLinked(user)) {
            EmbedBuilder builder = new EmbedBuilder()
                    .addField("Already linked.", "Your discord is already linked to the account " + bot.getSqlManager().getName(user) + ".", false)
                    .setColor(Color.RED);
            user.openPrivateChannel().queue(channel ->
                    channel.sendMessage(builder.build()).queue());
            return;
        }

        String query = "SELECT username, rank, uuid FROM users WHERE code=? AND used_code=?";
        try (PreparedStatement statement = bot.getSqlManager().getConnection().prepareStatement(query)) {
            statement.setString(1, message);
            statement.setInt(2, 0);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    String name = resultSet.getString("username");
                    String rank = resultSet.getString("rank");
                    String uuid = resultSet.getString("uuid");

                    query = "UPDATE users SET discord_id=?, used_code=? WHERE code=?";
                    try (PreparedStatement statement1 = bot.getSqlManager().getConnection().prepareStatement(query)) {
                        statement1.setString(1, user.getId());
                        statement1.setInt(2, 1);
                        statement1.setString(3, message);
                        statement1.executeUpdate();
                    }

                    EmbedBuilder builder = new EmbedBuilder()
                            .addField("Successfully linked.", "You have linked your Discord account to " + name + ".", false)
                            .setColor(Color.GREEN);
                    user.openPrivateChannel().queue(channel ->
                            channel.sendMessage(builder.build()).queue());

                    List<Role> rankRoles = bot.getRankRoles().get(rank);
                    List<Role> remove = RoleUtil.getRemoveList(member, rankRoles);

                    String prefix = rank.equals("Normal") ? "" : "[" + rank.replace("-", " ") + "] ";
                    guild.getController().setNickname(member, prefix + name).queue();
                    guild.getController().modifyMemberRoles(member, rankRoles, remove).queue();

                    bot.getRedisManager().publishLinked(uuid);
                } else {
                    EmbedBuilder builder = new EmbedBuilder()
                            .addField("Invalid code.", message + " is an invalid code.", false)
                            .setColor(Color.RED);
                    user.openPrivateChannel().queue(channel ->
                            channel.sendMessage(builder.build()).queue());
                }
            }
        } catch (Exception e) {
            EmbedBuilder builder = new EmbedBuilder()
                    .addField("An error occurred.", "Something went wrong while linking your account, try again later.", false)
                    .setColor(Color.RED);
            user.openPrivateChannel().queue(channel ->
                    channel.sendMessage(builder.build()).queue());
            e.printStackTrace();
        }
    }
}
