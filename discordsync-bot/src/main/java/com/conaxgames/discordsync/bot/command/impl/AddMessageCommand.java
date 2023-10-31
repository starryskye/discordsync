package com.conaxgames.discordsync.bot.command.impl;

import com.conaxgames.discordsync.bot.DiscordSyncBot;
import com.conaxgames.discordsync.bot.command.ICommand;
import com.conaxgames.discordsync.bot.message.RoleReactMessage;
import com.conaxgames.discordsync.bot.util.RoleUtil;
import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.core.entities.Emote;
import net.dv8tion.jda.core.entities.Role;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;

import java.util.List;

@RequiredArgsConstructor
public class AddMessageCommand implements ICommand {
    private final DiscordSyncBot bot;

    public boolean handle(User user, TextChannel channel, String args) {
        if (args.length() < 3) {
            return true;
        }

        if (!RoleUtil.hasRole(bot.getGuild().getMember(user),
                "Manager", "Developer", "Owner")) {
            return false;
        }

        String[] parts = args.split(" ");
        if (parts.length < 3) {
            return true;
        }

        List<Role> rolesByName = bot.getGuild().getRolesByName(parts[0], true);
        if (rolesByName.isEmpty()) {
            return true;
        }
        Role role = rolesByName.get(0);

        List<Emote> emotesByName = bot.getGuild().getEmotesByName(parts[1], true);
        if (emotesByName.isEmpty()) {
            return true;
        }
        Emote emote = emotesByName.get(0);

        StringBuilder message = new StringBuilder();
        for (int i = 2; i < parts.length; i++) {
            message.append(parts[i]).append(" ");
        }

        channel.sendMessage(message.toString().trim()).queue((newMessage) -> {
            newMessage.addReaction(emote).queue();
            bot.getReactionManager().addMessage(new RoleReactMessage(
                    newMessage.getId(),
                    role.getName()
            ));
        });

        return true;
    }
}
