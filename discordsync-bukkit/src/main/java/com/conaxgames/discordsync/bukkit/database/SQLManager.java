package com.conaxgames.discordsync.bukkit.database;

import com.conaxgames.discordsync.bukkit.DiscordSyncPlugin;
import lombok.Getter;
import org.apache.commons.lang3.RandomStringUtils;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Collections;
import java.util.List;

@Getter
public class SQLManager {
    private final DiscordSyncPlugin plugin;
    private Connection connection;

    public SQLManager(DiscordSyncPlugin plugin) {
        this.plugin = plugin;

        FileConfiguration config = plugin.getConfig();
        String host = config.getString("database.mysql.host");
        int port = config.getInt("database.mysql.port");
        String username = config.getString("database.mysql.username");
        String password = config.getString("database.mysql.password");
        String database = config.getString("database.mysql.database");
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
        List<String> queries = Collections.singletonList(
                "CREATE TABLE IF NOT EXISTS users ("
                + "id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,"
                + "uuid VARCHAR(36) NOT NULL,"
                + "username VARCHAR(32) NOT NULL,"
                + "discord_id VARCHAR(24),"
                + "code VARCHAR(8) NOT NULL,"
                + "used_code TINYINT(1) DEFAULT 0,"
                + "rank VARCHAR(32) NOT NULL"
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

    public String setupPlayer(Player player) {
        String random = RandomStringUtils.randomAlphanumeric(8);

        String query = "INSERT INTO users (uuid, username, code, rank) VALUES (?, ?, ?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, player.getUniqueId().toString());
            statement.setString(2, player.getName());
            statement.setString(3, random);
            statement.setString(4, plugin.getGroupProvider().getGroup(player.getUniqueId()));
            statement.executeUpdate();
            return random;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return "";
    }
}
