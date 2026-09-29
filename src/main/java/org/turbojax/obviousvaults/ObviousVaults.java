package org.turbojax.obviousvaults;

import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.block.Vault;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockDispenseLootEvent;
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
        // Saving the default config
        saveDefaultConfig();

        // Loading the DataManager
        this.dataManager = new YamlDataManager();
        dataManager.load();

        // Setting up the message system
        Message.setLangDir(new File("plugins/ObviousVaults/lang"));
        Message.loadBaseFiles(getClassLoader());
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
        dataManager.save();
        BlockDispenseLootEvent.getHandlerList().unregister((JavaPlugin) this);
    }

    @EventHandler
    public void onDispense(BlockDispenseLootEvent event) {
        Player player = event.getPlayer();
        Block block = event.getBlock();
        if (player == null) return;
        if (!(block.getState() instanceof Vault vault)) return;

        // If the plugin should dispense the displayed item, replace the first item in the list with it.
        if (getConfig().getBoolean("dispense_shown_item", true)) {
            ItemStack item = vault.getDisplayedItem();

            // Editing the loot
            var loot = event.getDispensedLoot();
            loot.set(loot.size() - 1, item);
            event.setDispensedLoot(loot);
        }

        // Getting the vault data
        var vaultData = (org.bukkit.block.data.type.Vault) block.getBlockData();

        // Getting usage limit and uses
        int usageLimit = getConfig().getInt((vaultData.isOminous() ? "ominous_usage_limit" : "usage_limit"), 1);
        int uses = dataManager.getUses(block.getLocation(), player.getUniqueId());

        // Handling when the player can still use the vault
        if (uses >= usageLimit) return;
        // Incrementing the usage count
        uses++;
        dataManager.setUses(block.getLocation(), player.getUniqueId(), uses);

        // Removing the player from the vault's rewarded players list after 20 ticks
        Bukkit.getScheduler().runTaskLater(this, () -> {
            Vault vaultState = (Vault) block.getLocation().getBlock().getState();
            vaultState.removeRewardedPlayer(player.getUniqueId());
            vaultState.update();
        }, 1);

        // Showing feedback if allowed
        if (!getConfig().getBoolean("show_usage_limit", true)) return;

        if (uses == usageLimit) {
            player.sendMessage(Objects.requireNonNullElse(Message.translatable(Messages.FINAL_USE), Component.empty()));
            return;
        }

        Message msg = Message.translatable(Messages.USAGE_LIMIT);
        if (msg != null) {
            msg.applyPlaceholder("uses", usageLimit - uses);
            player.sendMessage(msg);
        }
    }
}
