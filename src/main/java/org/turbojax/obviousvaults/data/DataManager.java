package org.turbojax.obviousvaults.data;

import org.bukkit.Location;

import java.util.UUID;

public interface DataManager {
    void load();
    void save();

    void setUses(Location pos, UUID playerId, int uses);
    int getUses(Location pos, UUID playerId);

    default void incrementUses(Location pos, UUID playerId) {
        setUses(pos, playerId, getUses(pos, playerId) + 1);
    }
}
