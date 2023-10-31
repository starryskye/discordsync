package com.conaxgames.discordsync.bot.message;

import com.conaxgames.discordsync.bot.DiscordSyncBot;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.dv8tion.jda.core.entities.Role;

import java.util.List;

@Getter
public class RoleReactMessage {
    private String messageId;
    private Role role;

    public RoleReactMessage(String messageId, String role) {
        this.messageId = messageId;

        List<Role> rolesByName =
                DiscordSyncBot.getInstance().getGuild().getRolesByName(role, true);
        if (rolesByName.isEmpty()) {
            throw new IllegalArgumentException("Invalid role " + role);
        }

        this.role = rolesByName.get(0);
    }
}
