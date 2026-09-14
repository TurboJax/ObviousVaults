package org.turbojax.elitesvault;

import org.bukkit.block.Vault;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockDispenseLootEvent;
import org.bukkit.inventory.ItemStack;

public class VaultListener implements Listener {
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
