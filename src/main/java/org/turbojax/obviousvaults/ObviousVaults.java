package org.turbojax.obviousvaults;

import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Vault;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockDispenseLootEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.turbojax.messages.Message;
import org.turbojax.obviousvaults.data.DataManager;
import org.turbojax.obviousvaults.data.YamlDataManager;

import java.io.File;
import java.util.*;

public final class ObviousVaults extends JavaPlugin implements Listener {
    public static final Logger LOGGER = LoggerFactory.getLogger("ObviousVaults");

    private final DataManager dataManager;

    public ObviousVaults() {
        // Loading the DataManager
        this.dataManager = new YamlDataManager();

        // Setting up the message system
        Message.setLangDir(new File("plugins/ObviousVaults/lang"));
    }

    @Override
    public void onEnable() {
        // Registering commands
        getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS, e -> {
            e.registrar().register("obviousvaults", (ctx, args) -> {
                if (!ctx.getSender().hasPermission("obviousvaults.reload")) {
                    Message msg = Message.translatable(Messages.NOT_PLAYER);
                    if (msg == null) return;
                    msg.applyPlaceholder("permission", "obviousvaults.reload");
                    ctx.getSender().sendMessage(msg);
                    return;
                }

                if (args.length == 0 || !args[0].equals("reload")) {
                    ctx.getSender().sendMessage(Objects.requireNonNullElse(Message.translatable(Messages.HELP_MESSAGE), Component.empty()));
                    return;
                }

                reloadConfig();
                ctx.getSender().sendMessage(Objects.requireNonNullElse(Message.translatable(Messages.RELOAD_SUCCESS), Component.empty()));
            });
        });

        // Registering event listeners
        Bukkit.getPluginManager().registerEvents(this, this);
    }

    @Override
    public void onDisable() {
        BlockDispenseLootEvent.getHandlerList().unregister((JavaPlugin) this);
    }

    @EventHandler
    public void onDispense(BlockDispenseLootEvent event) {
        if (!(event.getBlock().getState() instanceof Vault vault)) return;
        if (!getConfig().getBoolean("dispense_shown_item", true)) return;
        ItemStack item = vault.getDisplayedItem();

        // Editing the loot
        var loot = event.getDispensedLoot();
        loot.set(loot.size() - 1, item);
        event.setDispensedLoot(loot);

        // TODO: Implement if existing version doesn't work
        // If the player can still use the vault, remove them from the list of rewarded players
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        Block block = event.getClickedBlock();

        // Skipping if the player didn't interact with a vault
        if (block == null) return;
        if (block.getType() != Material.VAULT) return;

        // Getting the vault state and data
        Vault vaultState = (Vault) block.getState();
        var vaultData = (org.bukkit.block.data.type.Vault) block.getBlockData();

        // Getting usage limit and uses
        int usageLimit = getConfig().getInt((vaultData.isOminous() ? "ominous_usage_limit" : "usage_limit"), 1);
        int uses = dataManager.getUses(block.getLocation(), player.getUniqueId());

        // If the player can still use the vault, remove them from the list of rewarded players (should let the interaction go through)
        if (uses >= usageLimit) return;
        uses++;
        dataManager.setUses(block.getLocation(), player.getUniqueId(), uses);
        vaultState.removeRewardedPlayer(player.getUniqueId());

        // Sending feedback messages
        if (!getConfig().getBoolean("show_usage_limit", true)) return;

        if (uses == usageLimit) {
            player.sendMessage(Objects.requireNonNullElse(Message.translatable(Messages.FINAL_USE), Component.empty()));
            return;
        }

        Message msg = Message.translatable(Messages.USAGE_LIMIT);
        if (msg == null) return;
        msg.applyPlaceholder("uses", usageLimit - uses);
        player.sendMessage(msg);
    }
}
