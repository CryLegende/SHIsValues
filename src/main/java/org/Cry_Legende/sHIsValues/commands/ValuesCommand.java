package org.Cry_Legende.sHIsValues.commands;

import org.Cry_Legende.sHIsValues.SHIsValues;
import org.Cry_Legende.sHIsValues.ValuesGUI;
import org.Cry_Legende.sHIsValues.managers.ConfigManager;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

/**
 * /shisvalues (alias /shiv) : values, value (menu des valeurs de blocs) et reload.
 */
public class ValuesCommand implements CommandExecutor, TabCompleter {

    private static final String VALUES = "shisvalues.values";
    private static final String VALUE = "shisvalues.value";
    private static final String RELOAD = "shisvalues.admin.reload";

    private final SHIsValues plugin;
    private final ConfigManager configManager;
    private final ValuesGUI valuesGUI;

    public ValuesCommand(SHIsValues plugin) {
        this.plugin = plugin;
        this.configManager = plugin.getConfigManager();
        this.valuesGUI = plugin.getValuesGUI();
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        List<String> allowed = allowed(sender);
        if (allowed.isEmpty()) {
            sender.sendMessage(configManager.noPermissionMsg);
            return true;
        }
        String subCommand = args.length == 0 ? "" : args[0].toLowerCase(Locale.ROOT);
        if (!allowed.contains(subCommand)) {
            sender.sendMessage(configManager.message("usage", "%subcommands%", String.join("|", allowed)));
            return true;
        }

        switch (subCommand) {
            case "values" -> {
                if (!(sender instanceof Player player)) {
                    sender.sendMessage(configManager.playerOnlyMsg);
                    return true;
                }
                valuesGUI.openValuesInventory(player, 1);
            }
            case "value" -> {
                if (!(sender instanceof Player player)) {
                    sender.sendMessage(configManager.playerOnlyMsg);
                    return true;
                }
                ItemStack itemInHand = player.getInventory().getItemInMainHand();
                if (itemInHand.getType() == Material.AIR) {
                    player.sendMessage(configManager.itemNotFoundMsg);
                    return true;
                }
                int page = valuesGUI.getPageForItem(itemInHand.getType(), player);
                if (page != -1) {
                    valuesGUI.openValuesInventory(player, page);
                } else {
                    player.sendMessage(configManager.itemNotFoundMsg);
                }
            }
            default -> reload(sender);
        }
        return true;
    }

    private void reload(CommandSender sender) {
        try {
            configManager.reloadConfig();
        } catch (SHIsValues.ConfigException e) {
            plugin.getLogger().severe("Rechargement annulé, " + e.getFile() + " illisible : " + e.getMessage());
            sender.sendMessage(configManager.message("reload_failed", "%file%", e.getFile(), "%error%", String.valueOf(e.getMessage())));
            return;
        }
        plugin.getAccessList().loadPrivilegeNames();
        if (!plugin.getValuesManager().loadBlockValues()) {
            sender.sendMessage(configManager.message("reload_values_error"));
        } else {
            sender.sendMessage(configManager.configReloadedMsg);
        }
    }

    /** Sous-commandes que l'expéditeur a le droit d'utiliser. */
    private static List<String> allowed(CommandSender sender) {
        List<String> names = new ArrayList<>();
        if (sender.hasPermission(VALUES)) {
            names.add("values");
        }
        if (sender.hasPermission(VALUE)) {
            names.add("value");
        }
        if (sender.hasPermission(RELOAD)) {
            names.add("reload");
        }
        return names;
    }

    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        if (args.length == 1) {
            String currentArg = args[0].toLowerCase(Locale.ROOT);
            return allowed(sender).stream()
                .filter(s -> s.startsWith(currentArg))
                .collect(Collectors.toList());
        }
        return List.of();
    }
}
