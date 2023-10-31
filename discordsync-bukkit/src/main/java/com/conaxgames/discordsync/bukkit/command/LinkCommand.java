package com.conaxgames.discordsync.bukkit.command;

import com.conaxgames.discordsync.bukkit.DiscordSyncPlugin;
import com.conaxgames.discordsync.bukkit.MessageConfig;
import com.conaxgames.discordsync.bukkit.profile.Profile;
import lombok.RequiredArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

@RequiredArgsConstructor
public class LinkCommand implements CommandExecutor {
    private final DiscordSyncPlugin plugin;

    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            return true;
        }

        Player player = (Player) sender;
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            Profile profile = plugin.getProfileManager().getProfile(player);
            if (profile.getCode() != null) {
                if (!profile.isUsedCode()) {
                    for (String message : MessageConfig.UNUSED_CODE) {
                        player.sendMessage(ChatColor.translateAlternateColorCodes('&',
                                message.replace("%PLAYER%", player.getName())
                                        .replace("%CODE%", profile.getCode())));
                    }
                } else {
                    for (String message : MessageConfig.ALREADY_LINKED) {
                        player.sendMessage(ChatColor.translateAlternateColorCodes('&',
                                message.replace("%PLAYER%", player.getName())
                                        .replace("%CODE%", profile.getCode())));
                    }
                }

                return;
            }

            String code = plugin.getSqlManager().setupPlayer(player);
            if (!code.isEmpty()) {
                profile.setCode(code);
                for (String message : MessageConfig.CREATED_CODE) {
                    player.sendMessage(ChatColor.translateAlternateColorCodes('&',
                            message.replace("%PLAYER%", player.getName())
                                    .replace("%CODE%", code)));
                }

                return;
            }

            player.sendMessage(ChatColor.RED + "Something went wrong linking your account. Try again later.");
        });

        return true;
    }
}
