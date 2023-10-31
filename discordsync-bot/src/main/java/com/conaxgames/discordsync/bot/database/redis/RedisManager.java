package com.conaxgames.discordsync.bot.database.redis;

import com.conaxgames.discordsync.bot.DiscordSyncBot;
import com.google.gson.JsonObject;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

public class RedisManager {
    private String publishChannel;
    private JedisPool pool;

    public RedisManager(DiscordSyncBot bot) {
        String host = bot.getConfig().getString("redis.host");
        int port = Math.toIntExact(bot.getConfig().getLong("redis.port"));
        String auth = bot.getConfig().getString("redis.auth");
        this.publishChannel = bot.getConfig().getString("redis.channel");

        this.pool = new JedisPool(new JedisPoolConfig(), host, port, 0, auth);
    }

    public void publish(JsonObject object) {
        try (Jedis jedis = pool.getResource()) {
            jedis.publish(publishChannel, object.toString());
        }
    }

    public void publishLinked(String uuid) {
        JsonObject object = new JsonObject();
        object.addProperty("type", RedisMessageType.PLAYER_LINKED.name());
        object.addProperty("uuid", uuid);
        this.publish(object);
    }
}
