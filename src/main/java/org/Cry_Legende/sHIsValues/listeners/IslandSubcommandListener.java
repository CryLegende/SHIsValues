package org.Cry_Legende.sHIsValues.listeners;

import org.Cry_Legende.sHIsValues.SHIsValues;
import org.Cry_Legende.sHIsValues.ValuesGUI;
import org.Cry_Legende.sHIsValues.managers.ConfigManager;
import org.Cry_Legende.sHIsValues.managers.ValuesManager;
import org.Cry_Legende.sHIsValues.utils.GuiItemConfig;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.inventory.ItemStack;

public class IslandSubcommandListener implements Listener {

    private final SHIsValues plugin;
    private final ConfigManager configManager;
    private final ValuesManager valuesManager;
    private final ValuesGUI valuesGUI;

    public IslandSubcommandListener(SHIsValues plugin) {
        this.plugin = plugin;
        this.configManager = plugin.getConfigManager();
        this.valuesManager = plugin.getValuesManager();
        this.valuesGUI = plugin.getValuesGUI(); // Assure-toi que ValuesGUI est accessible
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    // Priorité haute pour intercepter avant les autres
    public void onPlayerCommand(PlayerCommandPreprocessEvent event) {
        // Sortie rapide : cet événement passe pour chaque commande de chaque joueur
        String rawMessage = event.getMessage();
        if (!rawMessage.regionMatches(true, 0, "/is ", 0, 4)) {
            return;
        }

        Player player = event.getPlayer();
        String commandMessage = rawMessage.toLowerCase(); // "/is values" ou "/is value"

        String[] parts = commandMessage.split(" ");
        if (parts.length < 1) return;

        String baseCommand = parts[0]; // Sera "/is"

        if (!baseCommand.equals("/is")) {
            return; // Ce n'est pas la commande /is
        }

        if (parts.length > 1) {
            String subCommand = parts[1];

            if (subCommand.equals("values")) {
                if (!player.hasPermission("shisvalues.command.is.values")) { // Permission spécifique
                    // Optionnel: envoyer un message d'erreur si tu veux indiquer que la sous-commande existe mais qu'il manque la perm
                    // player.sendMessage(configManager.noPermissionMsg);
                    return; // Laisse SSB2 ou un autre plugin gérer si le joueur n'a pas la perm pour ta sous-commande
                }
                event.setCancelled(true); // Annule l'événement pour que SSB2 ne le traite pas
                valuesGUI.openValuesInventory(player, 1);
                player.sendMessage(configManager.guiOpenedMsg);
                return; // Ta commande a été gérée

            } else if (subCommand.equals("value")) {
                if (!player.hasPermission("shisvalues.command.is.value")) { // Permission spécifique
                    return;
                }
                event.setCancelled(true); // Annule l'événement
                ItemStack itemInHand = player.getInventory().getItemInMainHand();

                if (itemInHand.getType() == Material.AIR) {
                    player.sendMessage(configManager.itemNotFoundMsg); // Ou un message "tenez un item"
                    return;
                }

                Material materialInHand = itemInHand.getType();
                int page = valuesManager.getPageForItem(materialInHand, determineItemsPerPageForPlayer(player));

                if (page != -1) {
                    valuesGUI.openValuesInventory(player, page);
                    player.sendMessage(configManager.guiOpenedMsg);
                } else {
                    player.sendMessage(configManager.itemNotFoundMsg);
                }
                return; // Ta commande a été gérée
            }
        }
        // Si ce n'est ni "/is values" ni "/is value", ou si la permission manque,
        // l'événement n'est PAS annulé, donc SuperiorSkyblock2 (ou un autre plugin)
        // peut traiter la commande /is normalement.
    }

    // Helper pour déterminer itemsPerPage car il peut être dynamique
    private int determineItemsPerPageForPlayer(Player player) {
        // Cette logique doit correspondre à celle de ValuesGUI.openValuesInventory
        // pour calculer effectiveItemsPerPage. C'est un peu redondant.
        // Idéalement, ValuesGUI pourrait avoir une méthode pour calculer ça.
        int itemsPerPage;
        if (!configManager.valueItemSlots.isEmpty()) {
            itemsPerPage = (int) configManager.valueItemSlots.stream()
                .filter(slot -> slot >= 0 && slot < (configManager.guiRows * 9) && !isSlotOccupiedByStatic(slot, player))
                .count();
        } else {
            itemsPerPage = (configManager.guiRows * 9) - countStaticOccupiedSlots(player);
        }
        return Math.max(1, itemsPerPage); // Au moins 1 pour éviter division par zéro
    }

    // Ces méthodes sont des duplicatas simplifiés de ValuesGUI. Elles devraient idéalement être centralisées.
    private boolean isSlotOccupiedByStatic(int slot, Player player) {
        for (GuiItemConfig staticItemConf : configManager.staticGuiItems.values()) {
            if (staticItemConf.isEnabled() && staticItemConf.getSlots().contains(slot)) {
                return true;
            }
        }
        return false;
    }

    private int countStaticOccupiedSlots(Player player) {
        java.util.Set<Integer> uniqueStaticSlots = new java.util.HashSet<>();
        for (GuiItemConfig staticItemConf : configManager.staticGuiItems.values()) {
            if (staticItemConf.isEnabled()) {
                uniqueStaticSlots.addAll(staticItemConf.getSlots());
            }
        }
        return uniqueStaticSlots.size();
    }
}
