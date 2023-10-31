package com.conaxgames.discordsync.bukkit.database.redis;

import com.conaxgames.discordsync.bukkit.DiscordSyncPlugin;
import com.conaxgames.discordsync.bukkit.event.DiscordLinkEvent;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.bukkit.Bukkit;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPubSub;

import java.util.UUID;

public class PluginSubscriber {
    private String host, auth, channel;
    private int port;

    private Jedis jedis;
    private JedisPubSub pubSub;

    public PluginSubscriber(DiscordSyncPlugin plugin) {
        this.host = plugin.getConfig().getString("database.redis.host");
        this.auth = plugin.getConfig().getString("database.redis.auth");
        this.channel = plugin.getConfig().getString("database.redis.channel");
        this.port = plugin.getConfig().getInt("database.redis.port");

        this.pubSub = new JedisPubSub() {
            public void onMessage(String channel, String message) {
                JsonElement element = new JsonParser().parse(message);
                JsonObject object = element.getAsJsonObject();
                RedisMessageType type = RedisMessageType.valueOf(object.get("type").getAsString());
                switch (type) {
                    case PLAYER_LINKED: {
                        UUID uuid = UUID.fromString(object.get("uuid").getAsString());
                        Bukkit.getScheduler().runTask(plugin,
                                () -> Bukkit.getPluginManager().callEvent(new DiscordLinkEvent(uuid)));
                        break;
                    }

                    default:
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
