package com.conaxgames.discordsync.bot.database.redis;

import com.conaxgames.discordsync.bot.DiscordSyncBot;
import com.conaxgames.discordsync.bot.util.RoleUtil;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Role;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPubSub;

import java.awt.*;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class BotSubscriber {
    private String host, auth, channel;
    private int port;

    private Jedis jedis;
    private JedisPubSub pubSub;

    public BotSubscriber(DiscordSyncBot bot) {
        this.host = bot.getConfig().getString("redis.host");
        this.auth = bot.getConfig().getString("redis.auth");
        this.channel = bot.getConfig().getString("redis.channel");
        this.port = Math.toIntExact(bot.getConfig().getLong("redis.port"));

        this.pubSub = new JedisPubSub() {
            public void onMessage(String channel, String message) {
                JsonElement element = new JsonParser().parse(message);
                JsonObject object = element.getAsJsonObject();
                RedisMessageType type = RedisMessageType.valueOf(object.get("type").getAsString());
                switch (type) {
                    case UPDATE_PLAYER: {
                        UUID uuid = UUID.fromString(object.get("uuid").getAsString());
                        String rank = object.get("rank").getAsString();

                        String query = "SELECT discord_id, rank, username FROM users WHERE uuid=?";
                        try (PreparedStatement statement = bot.getSqlManager().getConnection().prepareStatement(query)) {
                            statement.setString(1, uuid.toString());
                            try (ResultSet resultSet = statement.executeQuery()) {
                                if (resultSet.next()) {
                                    Member member = bot.getGuild().getMemberById(resultSet.getLong("discord_id"));
                                    String oldRank = resultSet.getString("rank");
                                    String name = resultSet.getString("username");

                                    if (member != null) {
                                        List<Role> rankRoles = bot.getRankRoles().get(rank);;
                                        List<Role> remove = RoleUtil.getRemoveList(member, rankRoles);

                                        String prefix = rank.equals("Normal") ? "" : "[" + rank.replace("-", " ") + "] ";
                                        bot.getGuild().getController().setNickname(member, prefix + name).queue();

                                        if (oldRank.equals(rank)) {
                                            if (!member.getRoles().equals(rankRoles)) {
                                                bot.getGuild().getController().modifyMemberRoles(member,
                                                        rankRoles,
                                                        remove).queue();
                                            }
                                        } else {
                                            bot.getGuild().getController().modifyMemberRoles(member,
                                                    rankRoles,
                                                    RoleUtil.getRemoveList(member, Collections.emptyList())).queue();
                                        }
                                    }
                                }
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        query = "UPDATE users SET rank=? WHERE uuid=?";
                        try (PreparedStatement statement = bot.getSqlManager().getConnection().prepareStatement(query)) {
                            statement.setString(1, rank);
                            statement.setString(2, uuid.toString());
                            statement.executeUpdate();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        break;
                    }

                    case UPDATE_NAME: {
                        UUID uuid = UUID.fromString(object.get("uuid").getAsString());
                        String name = object.get("name").getAsString();

                        String query = "UPDATE users SET username=? WHERE uuid=?";
                        try (PreparedStatement statement = bot.getSqlManager().getConnection().prepareStatement(query)) {
                            statement.setString(1, name);
                            statement.setString(2, uuid.toString());
                            statement.executeUpdate();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        query = "SELECT discord_id, rank FROM users WHERE uuid=?";
                        try (PreparedStatement statement = bot.getSqlManager().getConnection().prepareStatement(query)) {
                            statement.setString(1, uuid.toString());
                            try (ResultSet resultSet = statement.executeQuery()) {
                                if (resultSet.next()) {
                                    String rank = resultSet.getString("rank");
                                    Member member = bot.getGuild().getMemberById(resultSet.getLong("discord_id"));
                                    if (member != null) {
                                        String prefix = rank.equals("Normal") ? "" : "[" + rank.replace("-", " ") + "] ";
                                        bot.getGuild().getController().setNickname(member, prefix + name).queue();
                                    }
                                }
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        break;
                    }

                    case UHC_NOTIFY: {
                        String post = object.get("message").getAsString();
                        EmbedBuilder builder = new EmbedBuilder()
                                .setColor(Color.CYAN)
                                .addField("UHC Game", post, false);

                        bot.getUhcRole().getManager().setMentionable(true).queue();
                        bot.getUhcFeedChannel().sendMessage(bot.getUhcRole().getAsMention()).queue();
                        bot.getUhcFeedChannel().sendMessage(builder.build()).queue();
                        bot.getUhcRole().getManager().setMentionable(false).queue(); // wtf

                        break;
                    }

                    case PLAYER_LINKED: break;

                    default:
                        System.out.println("Unknown type " + object.get("type").getAsString());
                        break;
                }
            }
        };

        this.jedis = new Jedis(host, port);
        this.authenticate();
        this.connect();
    }

    private void authenticate() {
        if (!this.auth.isEmpty()) {
            this.jedis.auth(this.auth);
        }
    }

    private void connect() {
        new Thread(() -> {
            try {
                this.jedis.subscribe(this.pubSub, this.channel);
            } catch (Exception e) {
                e.printStackTrace();
                close();
                connect();
            }
        }).start();
    }

    public void close() {
        if (this.pubSub != null) {
            this.pubSub.unsubscribe();
        }

        if (this.jedis != null) {
            this.jedis.close();
        }
    }
}
