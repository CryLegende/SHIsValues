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
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class ValuesCommand implements CommandExecutor, TabCompleter {

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
        if (args.length == 0) {
            sendHelp(sender, label);
            return true;
        }

        String subCommand = args[0].toLowerCase();

        switch (subCommand) {
            case "values":
                if (!sender.hasPermission("shisvalues.command.values")) { // ou shisvalues.shiv.values
                    sender.sendMessage(configManager.noPermissionMsg);
                    return true;
                }
                if (!(sender instanceof Player)) {
                    sender.sendMessage(configManager.playerOnlyMsg);
                    return true;
                }
                Player player = (Player) sender;
                valuesGUI.openValuesInventory(player, 1);
                // Le message d'ouverture peut être géré dans openValuesInventory ou ici.
                // Si openValuesInventory ne l'envoie pas, décommentez :
                // player.sendMessage(configManager.guiOpenedMsg);
                return true;

            case "value":
                if (!sender.hasPermission("shisvalues.command.value")) { // ou shisvalues.shiv.value
                    sender.sendMessage(configManager.noPermissionMsg);
                    return true;
                }
                if (!(sender instanceof Player)) {
                    sender.sendMessage(configManager.playerOnlyMsg);
                    return true;
                }
                Player targetPlayer = (Player) sender;
                ItemStack itemInHand = targetPlayer.getInventory().getItemInMainHand();

                if (itemInHand.getType() == Material.AIR) {
                    targetPlayer.sendMessage(configManager.itemNotFoundMsg); // Ou "Tenez un item pour voir sa valeur."
                    return true;
                }

                Material materialInHand = itemInHand.getType();
                int page = valuesGUI.getPageForItem(materialInHand, targetPlayer);

                if (page != -1) {
                    valuesGUI.openValuesInventory(targetPlayer, page);
                    // targetPlayer.sendMessage(configManager.guiOpenedMsg);
                } else {
                    targetPlayer.sendMessage(configManager.itemNotFoundMsg);
                }
                return true;

            case "reload":
                if (!sender.hasPermission("shisvalues.command.reload")) { // ou shisvalues.shiv.reload
                    sender.sendMessage(configManager.noPermissionMsg);
                    return true;
                }
                plugin.getConfigManager().reloadConfig(); // Utiliser le getter pour ConfigManager
                if (!plugin.getValuesManager().loadBlockValues()) { // Utiliser le getter pour ValuesManager
                    sender.sendMessage(plugin.color("&cErreur lors du rechargement des valeurs de blocs. Vérifiez la console."));
                } else {
                    sender.sendMessage(configManager.configReloadedMsg);
                }
                return true;

            default:
                sendHelp(sender, label);
                return true;
        }
    }

    private void sendHelp(CommandSender sender, String label) {
        sender.sendMessage(plugin.color("&6--- SHIsValues Aide (" + label + ") ---"));
        sender.sendMessage(plugin.color("&e/" + label + " values &7- Ouvre le menu des valeurs."));
        sender.sendMessage(plugin.color("&e/" + label + " value &7- Ouvre le menu à la page de l'item en main."));
        if (sender.hasPermission("shisvalues.command.reload")) { // ou shisvalues.shiv.reload
            sender.sendMessage(plugin.color("&e/" + label + " reload &7- Recharge la configuration."));
        }
    }

    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        if (command.getName().equalsIgnoreCase("shiv")) { // Assurez-vous que la commande principale est "shiv" dans plugin.yml
            if (args.length == 1) {
                List<String> completions = new ArrayList<>(Arrays.asList("values", "value"));
                if (sender.hasPermission("shisvalues.command.reload")) { // ou shisvalues.shiv.reload
                    completions.add("reload");
                }
                // Filtrage pour l'auto-complétion
                String currentArg = args[0].toLowerCase();
                return completions.stream()
                    .filter(s -> s.toLowerCase().startsWith(currentArg))
                    .collect(Collectors.toList());
            }
        }
        return null; // Ou Collections.emptyList();
    }
}
