package org.turbojax.elitesvault;

import org.bukkit.Bukkit;
import org.bukkit.event.block.BlockDispenseLootEvent;
import org.bukkit.plugin.java.JavaPlugin;

public final class ElitesVault extends JavaPlugin {
    @Override
    public void onEnable() {
        Bukkit.getPluginManager().registerEvents(new VaultListener(), this);
    }

    @Override
    public void onDisable() {
        BlockDispenseLootEvent.getHandlerList().unregister(this);
    }
}
