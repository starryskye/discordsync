package com.conaxgames.discordsync.bukkit.profile;

import com.conaxgames.discordsync.bukkit.DiscordSyncPlugin;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.bukkit.entity.Player;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.concurrent.CompletableFuture;

@Getter @Setter
@RequiredArgsConstructor
public class Profile {
    private static final DiscordSyncPlugin plugin = DiscordSyncPlugin.getInstance();
    private final Player player;

    private String name, code;
    private boolean usedCode;

    public CompletableFuture<Profile> load() {
        return CompletableFuture.supplyAsync(() -> {
            String query = "SELECT username, code, used_code FROM users WHERE uuid = ?";
            try (PreparedStatement statement = plugin.getSqlManager().getConnection().prepareStatement(query)) {
                statement.setString(1, player.getUniqueId().toString());
                try (ResultSet resultSet = statement.executeQuery()) {
                    if (resultSet.next()) {
                        this.name = resultSet.getString("username");
                        this.code = resultSet.getString("code");
                        this.usedCode = resultSet.getBoolean("used_code");
                    } else {
                        this.name = player.getName();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            return this;
        });
    }
}
