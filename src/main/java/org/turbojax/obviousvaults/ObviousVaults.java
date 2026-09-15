package org.turbojax.obviousvaults;

import org.bukkit.Bukkit;
import org.bukkit.block.Vault;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockDispenseLootEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

public final class ObviousVaults extends JavaPlugin implements Listener {
    @Override
    public void onEnable() {
        Bukkit.getPluginManager().registerEvents(this, this);
    }

    @Override
    public void onDisable() {
        BlockDispenseLootEvent.getHandlerList().unregister((JavaPlugin) this);
    }

    @EventHandler
    public void onDispense(BlockDispenseLootEvent event) {
        if (!(event.getBlock().getState() instanceof Vault vault)) return;
        ItemStack item = vault.getDisplayedItem();

        // Editing the loot
        var loot = event.getDispensedLoot();
        loot.set(loot.size() - 1, item);
        event.setDispensedLoot(loot);
    }
}
