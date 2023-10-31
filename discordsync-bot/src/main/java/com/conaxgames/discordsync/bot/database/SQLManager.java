package com.conaxgames.discordsync.bot.database;

import com.conaxgames.discordsync.bot.DiscordSyncBot;
import com.conaxgames.discordsync.bot.message.RoleReactMessage;
import lombok.Getter;
import net.dv8tion.jda.core.entities.User;

import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Getter
public class SQLManager {
    private final DiscordSyncBot bot;
    private Connection connection;

    public SQLManager(DiscordSyncBot bot) {
        this.bot = bot;

        String host = bot.getConfig().getString("mysql.host");
        String database = bot.getConfig().getString("mysql.database");
        String username = bot.getConfig().getString("mysql.username");
        String password = bot.getConfig().getString("mysql.password");
        int port = Math.toIntExact(bot.getConfig().getLong("mysql.port"));
        try {
            this.connection = DriverManager.getConnection(
                    "jdbc:mysql://" + host + ":" + port + "/" + database,
                    username,
                    password
            );
        } catch (Exception e) {
            e.printStackTrace();
        }

        this.createTables();
    }

    private void createTables() {
        List<String> queries = Arrays.asList(
                "CREATE TABLE IF NOT EXISTS users ("
                + "id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,"
                + "uuid VARCHAR(36) NOT NULL,"
                + "username VARCHAR(32) NOT NULL,"
                + "discord_id VARCHAR(24),"
                + "code VARCHAR(8) NOT NULL,"
                + "used_code TINYINT(1) DEFAULT 0,"
                + "rank VARCHAR(32) NOT NULL"
                + ") ENGINE=InnoDB DEFAULT CHARSET=latin1;",
                "CREATE TABLE IF NOT EXISTS messages ("
                + "id VARCHAR(20) NOT NULL PRIMARY KEY,"
                + "emoji VARCHAR(32) NOT NULL,"
                + "role VARCHAR(32) NOT NULL"
                + ") ENGINE=InnoDB DEFAULT CHARSET=latin1;"
        );

        queries.forEach(query -> {
            try (PreparedStatement statement = connection.prepareStatement(query)) {
                statement.executeUpdate();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    public boolean isLinked(User user) {
        String query = "SELECT * FROM users WHERE discord_id = ?";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, user.getId());
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    public String getRank(User user) {
        String query = "SELECT rank FROM users WHERE discord_id = ?";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, user.getId());
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getString("rank");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public String getName(User user) {
        String query = "SELECT username FROM users WHERE discord_id = ?";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, user.getId());
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getString("username");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public List<RoleReactMessage> getMessages() {
        List<RoleReactMessage> messages = new ArrayList<>();

        String query = "SELECT * FROM messages";
        try (Statement statement = connection.createStatement()) {
            try (ResultSet set = statement.executeQuery(query)) {
                while (set.next()) {
                    String id = set.getString("id");
                    String role = set.getString("role");

                    messages.add(new RoleReactMessage(id, role));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return messages;
    }

    public void addMessage(RoleReactMessage message) {
        String query = "INSERT INTO messages (`id`, `role`) VALUES (?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, message.getMessageId());
            statement.setString(2, message.getRole().getName());
            statement.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
