package com.conaxgames.discordsync.bot;

import com.conaxgames.discordsync.bot.command.ICommand;
import com.conaxgames.discordsync.bot.command.impl.AddMessageCommand;
import com.conaxgames.discordsync.bot.command.impl.AnnounceCommand;
import com.conaxgames.discordsync.bot.command.impl.RoleCommand;
import com.conaxgames.discordsync.bot.database.SQLManager;
import com.conaxgames.discordsync.bot.database.redis.BotSubscriber;
import com.conaxgames.discordsync.bot.database.redis.RedisManager;
import com.conaxgames.discordsync.bot.listener.MessageListener;
import com.conaxgames.discordsync.bot.listener.ReactionListener;
import com.conaxgames.discordsync.bot.listener.ReadyListener;
import com.conaxgames.discordsync.bot.message.ReactionMessageManager;
import com.moandjiezana.toml.Toml;
import lombok.Getter;
import lombok.Setter;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.JDABuilder;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Role;
import net.dv8tion.jda.core.entities.TextChannel;

import javax.security.auth.login.LoginException;
import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.*;

@Getter @Setter
public class DiscordSyncBot {
    @Getter private static DiscordSyncBot instance;

    private Toml config;

    private Map<String, List<Role>> rankRoles;
    private Map<String, ICommand> commandMap;

    private BotSubscriber botSubscriber;
    private SQLManager sqlManager;
    private RedisManager redisManager;
    private ReactionMessageManager reactionManager;

    private JDA jda;
    private Guild guild;
    private Role verifiedRole, playerRole;

    private TextChannel uhcFeedChannel;
    private Role uhcRole;

    DiscordSyncBot() {
        DiscordSyncBot.instance = this;

        File configFile = new File("config.toml");
        if (!configFile.exists()) {
            try (InputStream in = getClass().getClassLoader().getResourceAsStream("config.toml")) {
                Files.copy(in, configFile.toPath());
                System.exit(0);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        this.rankRoles = new HashMap<>();

        this.commandMap = new HashMap<>();
        this.commandMap.put("announce", new AnnounceCommand(this));
        this.commandMap.put("react", new AddMessageCommand(this));
        this.commandMap.put("role", new RoleCommand(this));

        this.config = new Toml().read(configFile);
        this.botSubscriber = new BotSubscriber(this);
        this.sqlManager = new SQLManager(this);
        this.redisManager = new RedisManager(this);
        this.reactionManager = new ReactionMessageManager(this);

        try {
            this.jda = new JDABuilder(this.config.getString("discord.token"))
                    .addEventListener(new MessageListener(this))
                    .addEventListener(new ReadyListener(this))
                    .addEventListener(new ReactionListener(this))
                    .build();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
