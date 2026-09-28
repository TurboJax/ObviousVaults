package org.turbojax.obviousvaults.data;

import net.kyori.adventure.key.Key;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.file.YamlConfiguration;
import org.turbojax.obviousvaults.ObviousVaults;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class YamlDataManager implements DataManager {
    private static final File file = new File("plugins/ObviousVaults/data.yml");

    private final Map<String, Integer> uses = new HashMap<>();

    @Override
    public void load() {
        if (!createFile()) return;

        uses.clear();

        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);

        var usesSection = config.getConfigurationSection("uses");
        if (usesSection != null) {
            usesSection.getKeys(false).forEach(key -> {
                int uses = config.getInt(key);
                this.uses.put(key, uses);
            });
        }
    }

    @Override
    public void save() {
        if (!createFile()) return;

        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
        config.set("uses", uses);

        try {
            config.save(file);
        } catch (IOException e) {
            ObviousVaults.LOGGER.error("Failed to save data file", e);
        }
    }

    @SuppressWarnings({"BooleanMethodIsAlwaysInverted", "ResultOfMethodCallIgnored"})
    public boolean createFile() {
        if (file.exists()) return true;

        try {
            file.getParentFile().mkdirs();
            file.createNewFile();
            ObviousVaults.LOGGER.debug("Created data file");
            return true;
        } catch (IOException e) {
            ObviousVaults.LOGGER.error("Failed to create data file", e);
            return false;
        }
    }

    private String getUsesKey(Location pos, UUID playerId) {
        return pos.getWorld().key() + "^" + pos.getBlockX() + "^" + pos.getBlockY() + "^" + pos.getBlockZ() + "^" + playerId.toString();
    }

    // May not need...
    private Location locFromUsesKey(String key) {
        String[] parts = key.split("\\^");
        return new Location(Bukkit.getWorld(Key.key(parts[0])), Integer.parseInt(parts[0]), Integer.parseInt(parts[1]), Integer.parseInt(parts[2]));
    }

    private UUID idFromUsesKey(String key) {
        return UUID.fromString(key.split("\\^")[4]);
    }

    @Override
    public void setUses(Location pos, UUID playerId, int uses) {
        this.uses.put(getUsesKey(pos, playerId), uses);
    }

    @Override
    public int getUses(Location pos, UUID playerId) {
        return this.uses.getOrDefault(getUsesKey(pos, playerId), 0);
    }
}
