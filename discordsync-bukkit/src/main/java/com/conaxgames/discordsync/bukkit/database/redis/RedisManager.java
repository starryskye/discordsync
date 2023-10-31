package com.conaxgames.discordsync.bukkit.database.redis;

import com.conaxgames.discordsync.bukkit.DiscordSyncPlugin;
import com.google.gson.JsonObject;
import org.bukkit.configuration.file.FileConfiguration;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.util.UUID;

public class RedisManager {
    private final DiscordSyncPlugin plugin;
    private String publishChannel;
    private JedisPool pool;

    public RedisManager(DiscordSyncPlugin plugin) {
        this.plugin = plugin;

        FileConfiguration config = plugin.getConfig();
        String host = config.getString("database.redis.host");
        int port = config.getInt("database.redis.port");
        String auth = config.getString("database.redis.auth");
        this.publishChannel = config.getString("database.redis.channel");

        this.pool = new JedisPool(new JedisPoolConfig(), host, port, 0, auth);
    }

    public void publish(JsonObject object) {
        try (Jedis jedis = pool.getResource()) {
            jedis.publish(publishChannel, object.toString());
        }
    }

    public void updatePlayer(UUID uuid, String rank) {
        JsonObject object = new JsonObject();
        object.addProperty("type", RedisMessageType.UPDATE_PLAYER.name());
        object.addProperty("uuid", uuid.toString());
        object.addProperty("rank", rank);
        plugin.getRedisManager().publish(object);
    }

    public void updateName(UUID uuid, String name) {
        JsonObject object = new JsonObject();
        object.addProperty("type", RedisMessageType.UPDATE_NAME.name());
        object.addProperty("uuid", uuid.toString());
        object.addProperty("name", name);
        plugin.getRedisManager().publish(object);
    }
}
