package com.conaxgames.discordsync.bukkit.profile;

import lombok.Getter;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.WeakHashMap;

@Getter
public class ProfileManager {
    private final Map<Player, Profile> profiles = new WeakHashMap<>();

    public Profile createPlayer(Player player) {
        Profile profile = new Profile(player);
        profiles.put(player, profile);
        return profile;
    }

    public Profile getProfile(Player player) {
        return profiles.get(player);
    }

    public void removePlayer(Player player) {
        profiles.remove(player);
    }
}
