package org.Cry_Legende.sHIsValues;

import me.clip.placeholderapi.PlaceholderAPI;
import org.Cry_Legende.sHIsValues.managers.ConfigManager;
import org.Cry_Legende.sHIsValues.managers.ValuesManager;
import org.Cry_Legende.sHIsValues.utils.BlockValue;
import org.Cry_Legende.sHIsValues.utils.GuiItemConfig;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag; // Importation nécessaire
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class ValuesGUI implements Listener {

    private final SHIsValues plugin;
    private final ConfigManager configManager;
    private final ValuesManager valuesManager;
    private final Map<UUID, Integer> playerPages = new HashMap<>();
    private final Map<UUID, String> openInventories = new HashMap<>();

    private final DecimalFormat valueFormatter;

    public ValuesGUI(SHIsValues plugin) {
        this.plugin = plugin;
        this.configManager = plugin.getConfigManager();
        this.valuesManager = plugin.getValuesManager();
        this.valueFormatter = new DecimalFormat("#0.####");
    }

    private ItemStack createConfiguredItemStack(Player player, GuiItemConfig itemConfig) {
        ItemStack itemStack = new ItemStack(itemConfig.getMaterial());
        if (itemConfig.getMaterial() == Material.AIR) return itemStack;

        ItemMeta meta = itemStack.getItemMeta();
        if (meta != null) {
            String name = itemConfig.getName();
            List<String> lore = new ArrayList<>();

            if (plugin.isPlaceholderApiHooked()) {
                name = PlaceholderAPI.setPlaceholders(player, name);
                for (String originalLoreLine : itemConfig.getLore()) {
                    lore.add(PlaceholderAPI.setPlaceholders(player, originalLoreLine));
                }
            } else {
                lore.addAll(itemConfig.getLore());
            }
            meta.setDisplayName(name);
            meta.setLore(lore);
            meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES); // Cacher les attributs
            // Tu peux ajouter d'autres ItemFlags ici si besoin (HIDE_ENCHANTS, HIDE_POTION_EFFECTS, etc.)
            itemStack.setItemMeta(meta);
        }
        return itemStack;
    }

    private String formatValue(double value) {
        if (value == (long) value) {
            return String.format("%d", (long) value);
        } else {
            return valueFormatter.format(value);
        }
    }

    private ItemStack createBlockValueItemStack(Player player, BlockValue blockValue) {
        Material material = blockValue.getMaterial();
        int amount = 1;

        String amountStringConfig = configManager.blockValueItemAmount;
        String processedAmountString = amountStringConfig;

        if (plugin.isPlaceholderApiHooked()) {
            if (amountStringConfig.contains("%superior_island_count_{material_id}%")) {
                String papiPlaceholderForCount = "%superior_island_count_" + material.name() + "%";
                processedAmountString = amountStringConfig.replace("%superior_island_count_{material_id}%", papiPlaceholderForCount);
            }
            String resolvedAmountStr = PlaceholderAPI.setPlaceholders(player, processedAmountString);
            try {
                double rawAmount = Double.parseDouble(resolvedAmountStr);
                amount = (int) Math.round(rawAmount);
            } catch (NumberFormatException e) {
                // Silencieux
            }
        } else {
            try {
                amount = Integer.parseInt(amountStringConfig);
            } catch (NumberFormatException e) {
                // Silencieux
            }
        }
        amount = Math.max(1, Math.min(amount, 64));

        ItemStack item = new ItemStack(material, amount);
        ItemMeta meta = item.getItemMeta();

        if (meta != null) {
            List<String> finalLore = new ArrayList<>();
            String itemValueFormatted = formatValue(blockValue.getValue());

            for (String loreLineTemplate : configManager.blockValueItemLore) {
                String processedLine = loreLineTemplate
                    .replace("%value%", itemValueFormatted)
                    .replace("%material_id%", material.name());

                if (plugin.isPlaceholderApiHooked()) {
                    if (processedLine.contains("%superior_island_count_{material_id}%")) {
                        String placeholderPapiCount = "%superior_island_count_" + material.name() + "%";
                        processedLine = processedLine.replace("%superior_island_count_{material_id}%", placeholderPapiCount);
                    }
                    if (processedLine.contains("{value}") || processedLine.contains("{material_id}")) {
                        String expressionAvecValeursBrutes = processedLine
                            .replace("{value}", String.valueOf(blockValue.getValue()))
                            .replace("{material_id}", material.name());
                        processedLine = expressionAvecValeursBrutes;
                    }
                    processedLine = PlaceholderAPI.setPlaceholders(player, processedLine);
                }
                finalLore.add(processedLine);
            }
            meta.setLore(finalLore);
            meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES); // Cacher les attributs
            // Tu peux ajouter d'autres ItemFlags ici si besoin
            item.setItemMeta(meta);
        }
        return item;
    }

    // ... openValuesInventory, onInventoryClick, onInventoryClose, calculateEffectiveItemsPerPage, getPageForItem, formatMaterialName ...
    // Le reste de la classe est identique à la version précédente.
    // Je vais le recoller pour la complétude.

    public void openValuesInventory(Player player, int page) {
        List<BlockValue> allBlockValues = valuesManager.getBlockValues();

        boolean hasContentToShow = !allBlockValues.isEmpty() || !configManager.staticGuiItems.isEmpty();
        if (!hasContentToShow) {
            player.sendMessage(configManager.noValuesToDisplayMsg);
            return;
        }
        if (allBlockValues.isEmpty() && !configManager.staticGuiItems.isEmpty()) {
            player.sendMessage(configManager.noValuesToDisplayMsg);
        }

        Inventory gui = Bukkit.createInventory(null, configManager.guiRows * 9, "TempTitle");
        Set<Integer> occupiedSlots = new HashSet<>();

        for (GuiItemConfig staticItemConf : configManager.staticGuiItems.values()) {
            if (!staticItemConf.isEnabled()) continue;
            ItemStack itemStack = createConfiguredItemStack(player, staticItemConf);
            for (int slot : staticItemConf.getSlots()) {
                if (slot >= 0 && slot < gui.getSize()) {
                    if (gui.getItem(slot) != null) {
                        plugin.getLogger().warning("Conflit de slot pour item statique! Slot " + slot + " (pour " + staticItemConf.getId() + ") est déjà occupé par '" + gui.getItem(slot).getType() + "'. L'item '" + itemStack.getType() + "' va l'écraser.");
                    }
                    gui.setItem(slot, itemStack);
                    occupiedSlots.add(slot);
                } else {
                    plugin.getLogger().warning("Slot " + slot + " pour item statique '" + staticItemConf.getId() + "' est hors limites (0-" + (gui.getSize() - 1) + ").");
                }
            }
        }

        List<Integer> availableValueSlots;
        int effectiveItemsPerPage;

        if (!configManager.valueItemSlots.isEmpty()) {
            availableValueSlots = new ArrayList<>();
            for (int configuredSlot : configManager.valueItemSlots) {
                if (configuredSlot >= 0 && configuredSlot < gui.getSize() && !occupiedSlots.contains(configuredSlot)) {
                    availableValueSlots.add(configuredSlot);
                } else if (occupiedSlots.contains(configuredSlot)) {
                    plugin.getLogger().info("Slot configuré " + configuredSlot + " dans 'value_item_slots' est déjà occupé par un item statique. Il sera ignoré pour les items de valeur.");
                } else {
                    plugin.getLogger().warning("Slot configuré " + configuredSlot + " dans 'value_item_slots' est hors limites (0-" + (gui.getSize() - 1) + "). Il sera ignoré.");
                }
            }
            effectiveItemsPerPage = availableValueSlots.size();
        } else {
            availableValueSlots = new ArrayList<>();
            for (int i = 0; i < gui.getSize(); i++) {
                if (!occupiedSlots.contains(i)) {
                    availableValueSlots.add(i);
                }
            }
            Collections.sort(availableValueSlots);
            effectiveItemsPerPage = availableValueSlots.size();
        }

        if (effectiveItemsPerPage == 0 && !allBlockValues.isEmpty()) {
            player.sendMessage(configManager.noValueSlotsConfiguredMsg);
        }

        int totalBlockItems = allBlockValues.size();
        int maxPages = (effectiveItemsPerPage > 0) ? (int) Math.ceil((double) totalBlockItems / effectiveItemsPerPage) : 1;
        if (totalBlockItems == 0) maxPages = 1;

        page = Math.max(1, Math.min(page, maxPages));

        if (effectiveItemsPerPage > 0 && totalBlockItems > 0) {
            int startIndex = (page - 1) * effectiveItemsPerPage;
            int endIndex = Math.min(startIndex + effectiveItemsPerPage, totalBlockItems);

            for (int i = 0; i < (endIndex - startIndex); i++) {
                if (i >= availableValueSlots.size()) {
                    plugin.getLogger().warning("Moins de slots disponibles (" + availableValueSlots.size() + ") que d'items à placer (" + (endIndex - startIndex) + ") sur la page " + page + ". Arrêt du placement des items de valeur.");
                    break;
                }
                BlockValue blockValue = allBlockValues.get(startIndex + i);
                int targetSlot = availableValueSlots.get(i);
                gui.setItem(targetSlot, createBlockValueItemStack(player, blockValue));
            }
        }

        String title = configManager.guiTitle
            .replace("%page%", String.valueOf(page))
            .replace("%max_pages%", String.valueOf(maxPages));
        if (plugin.isPlaceholderApiHooked()) {
            title = PlaceholderAPI.setPlaceholders(player, title);
        }
        Inventory finalGui = Bukkit.createInventory(null, gui.getSize(), title);
        finalGui.setContents(gui.getContents());

        playerPages.put(player.getUniqueId(), page);
        openInventories.put(player.getUniqueId(), title);
        player.openInventory(finalGui);
    }


    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        String currentViewTitle = event.getView().getTitle();

        if (!openInventories.containsKey(player.getUniqueId()) ||
            !currentViewTitle.equals(openInventories.get(player.getUniqueId()))) {
            return;
        }

        event.setCancelled(true);
        ItemStack clickedItem = event.getCurrentItem();
        if (clickedItem == null || clickedItem.getType() == Material.AIR) return;

        int clickedSlot = event.getRawSlot();
        if (clickedSlot >= event.getInventory().getSize()) return;

        int currentPage = playerPages.getOrDefault(player.getUniqueId(), 1);

        for (GuiItemConfig staticItemConf : configManager.staticGuiItems.values()) {
            if (!staticItemConf.isEnabled() || staticItemConf.getAction() == null) continue;
            if (staticItemConf.getSlots().contains(clickedSlot)) {
                ItemStack expectedItem = createConfiguredItemStack(player, staticItemConf);
                if (!clickedItem.isSimilar(expectedItem)) {
                    if (clickedItem.getType() != staticItemConf.getMaterial()) {
                        continue;
                    }
                }

                if ("PREVIOUS_PAGE".equalsIgnoreCase(staticItemConf.getAction())) {
                    if (currentPage > 1) {
                        openValuesInventory(player, currentPage - 1);
                    }
                    return;
                } else if ("NEXT_PAGE".equalsIgnoreCase(staticItemConf.getAction())) {
                    int itemsPerPageForCalc = calculateEffectiveItemsPerPage(player);
                    if (itemsPerPageForCalc <= 0) itemsPerPageForCalc = 1;

                    int totalItems = valuesManager.getBlockValues().size();
                    int maxPagesCalc = (int) Math.ceil((double) totalItems / itemsPerPageForCalc);
                    if (maxPagesCalc == 0 && (totalItems > 0 || !configManager.staticGuiItems.isEmpty() && hasVisibleStaticItems()))
                        maxPagesCalc = 1;
                    if (maxPagesCalc == 0 && totalItems == 0 && (configManager.staticGuiItems.isEmpty() || !hasVisibleStaticItems()))
                        maxPagesCalc = 1;


                    if (currentPage < maxPagesCalc) {
                        openValuesInventory(player, currentPage + 1);
                    }
                    return;
                }
            }
        }
    }

    private boolean hasVisibleStaticItems() {
        return configManager.staticGuiItems.values().stream().anyMatch(GuiItemConfig::isEnabled);
    }

    private int calculateEffectiveItemsPerPage(Player playerContext) {
        Set<Integer> tempOccupiedSlots = new HashSet<>();
        for (GuiItemConfig staticItemConf : configManager.staticGuiItems.values()) {
            if (staticItemConf.isEnabled()) {
                tempOccupiedSlots.addAll(staticItemConf.getSlots());
            }
        }

        if (!configManager.valueItemSlots.isEmpty()) {
            int count = 0;
            for (int configuredSlot : configManager.valueItemSlots) {
                if (configuredSlot >= 0 && configuredSlot < (configManager.guiRows * 9) && !tempOccupiedSlots.contains(configuredSlot)) {
                    count++;
                }
            }
            return count;
        } else {
            int count = 0;
            for (int i = 0; i < (configManager.guiRows * 9); i++) {
                if (!tempOccupiedSlots.contains(i)) {
                    count++;
                }
            }
            return count;
        }
    }


    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        Player player = (Player) event.getPlayer();
        if (openInventories.containsKey(player.getUniqueId()) &&
            event.getView().getTitle().equals(openInventories.get(player.getUniqueId()))) {
            playerPages.remove(player.getUniqueId());
            openInventories.remove(player.getUniqueId());
        }
    }

    public int getPageForItem(Material material, Player playerContext) {
        List<BlockValue> allBlockValues = valuesManager.getBlockValues();
        if (allBlockValues.isEmpty()) {
            return -1;
        }

        int effectiveItemsPerPage = calculateEffectiveItemsPerPage(playerContext);
        if (effectiveItemsPerPage <= 0) {
            plugin.getLogger().warning("getPageForItem: effectiveItemsPerPage est <= 0 (" + effectiveItemsPerPage + "). Impossible de déterminer la page.");
            return -1;
        }

        for (int i = 0; i < allBlockValues.size(); i++) {
            if (allBlockValues.get(i).getMaterial() == material) {
                return (i / effectiveItemsPerPage) + 1;
            }
        }
        return -1;
    }

    private String formatMaterialName(Material material) {
        String name = material.name().toLowerCase().replace('_', ' ');
        String[] words = name.split(" ");
        StringBuilder formattedName = new StringBuilder();
        for (String word : words) {
            if (!word.isEmpty()) {
                formattedName.append(Character.toUpperCase(word.charAt(0)))
                    .append(word.substring(1))
                    .append(" ");
            }
        }
        return formattedName.toString().trim();
    }
}
