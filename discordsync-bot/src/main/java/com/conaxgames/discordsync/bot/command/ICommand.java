package com.conaxgames.discordsync.bot.command;

import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;

public interface ICommand {
    boolean handle(User user, TextChannel channel, String args);
}
