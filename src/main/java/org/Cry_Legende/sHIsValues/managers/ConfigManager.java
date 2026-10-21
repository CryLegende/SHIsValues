package org.Cry_Legende.sHIsValues.managers;

import org.Cry_Legende.sHIsValues.SHIsValues;
import org.Cry_Legende.sHIsValues.utils.GuiItemConfig;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ConfigManager {

    private final SHIsValues plugin;
    private FileConfiguration config;

    // GUI Settings
    public String guiTitle;
    public int guiRows;
    public List<Integer> valueItemSlots;
    public int itemsPerPage; // Sera dérivé de valueItemSlots.size() ou du remplissage auto

    public Map<String, GuiItemConfig> staticGuiItems;
    public List<String> blockValueItemLore;
    public String blockValueItemAmount; // Chaîne pour la quantité, peut être un placeholder

    // Messages
    public String noPermissionMsg, playerOnlyMsg, guiOpenedMsg, itemNotFoundMsg,
        ssb2ValuesNotFoundMsg, ssb2ValuesErrorMsg, configReloadedMsg,
        noValueSlotsConfiguredMsg, noValuesToDisplayMsg;

    public String ssb2ValuesPath;

    public ConfigManager(SHIsValues plugin) {
        this.plugin = plugin;
        this.staticGuiItems = new HashMap<>();
    }

    public void loadConfig() {
        plugin.reloadConfig(); // S'assure que la dernière version est chargée depuis le disque
        config = plugin.getConfig();
        staticGuiItems.clear(); // Nettoyer avant de recharger

        guiTitle = plugin.color(config.getString("gui.title", "&2Valeurs des Blocs (Page %page%/%max_pages%)"));
        guiRows = Math.max(1, Math.min(6, config.getInt("gui.rows", 6)));

        valueItemSlots = config.getIntegerList("gui.value_item_slots");
        if (valueItemSlots == null) {
            valueItemSlots = new ArrayList<>();
        }

        // Charger les items statiques du GUI
        ConfigurationSection itemsSection = config.getConfigurationSection("gui.items");
        if (itemsSection != null) {
            for (String key : itemsSection.getKeys(false)) {
                ConfigurationSection itemConfig = itemsSection.getConfigurationSection(key);
                if (itemConfig == null) continue;

                boolean enabled = itemConfig.getBoolean("enabled", true);
                Material material = Material.matchMaterial(itemConfig.getString("material", "STONE"));
                if (material == null) {
                    plugin.getLogger().warning("Matériau invalide '" + itemConfig.getString("material") + "' pour l'item statique '" + key + "'. Utilisation de STONE par défaut.");
                    material = Material.STONE;
                }
                String name = plugin.color(itemConfig.getString("name", " "));
                List<String> lore = itemConfig.getStringList("lore").stream().map(plugin::color).collect(Collectors.toList());
                List<Integer> slots = itemConfig.getIntegerList("slots");
                if (slots == null) slots = new ArrayList<>();
                String action = itemConfig.getString("action");

                staticGuiItems.put(key, new GuiItemConfig(key, enabled, material, name, lore, slots, action));
            }
        }

        // itemsPerPage est calculé dynamiquement dans ValuesGUI basé sur valueItemSlots et les items statiques.
        // On ne le définit plus fixement ici à moins d'une logique spécifique.
        itemsPerPage = 0; // Sera recalculé dans ValuesGUI

        // Configuration pour block_value_item
        blockValueItemLore = config.getStringList("gui.block_value_item.lore").stream().map(plugin::color).collect(Collectors.toList());
        blockValueItemAmount = config.getString("gui.block_value_item.amount", "1");

        // Messages
        noPermissionMsg = plugin.color(config.getString("messages.no_permission", "&cNo permission."));
        playerOnlyMsg = plugin.color(config.getString("messages.player_only", "&cPlayer only."));
        guiOpenedMsg = plugin.color(config.getString("messages.gui_opened", "&aGUI opened."));
        itemNotFoundMsg = plugin.color(config.getString("messages.item_not_found_in_values", "&cItem not found in values."));
        ssb2ValuesNotFoundMsg = plugin.color(config.getString("messages.ssb2_values_not_found", "&cSSB2 values not found."));
        ssb2ValuesErrorMsg = plugin.color(config.getString("messages.ssb2_values_error", "&cError reading SSB2 values."));
        configReloadedMsg = plugin.color(config.getString("messages.config_reloaded", "&aConfig reloaded."));
        noValueSlotsConfiguredMsg = plugin.color(config.getString("messages.no_value_slots_configured", "&cNo slots for value items."));
        noValuesToDisplayMsg = plugin.color(config.getString("messages.no_values_to_display", "&cNo values to display."));

        ssb2ValuesPath = config.getString("superior_skyblock_values_path", "plugins/SuperiorSkyblock2/block-values/levels.yml");
    }

    public GuiItemConfig getStaticItemConfig(String id) {
        return staticGuiItems.get(id);
    }

    public void reloadConfig() {
        plugin.saveDefaultConfig(); // S'assure que le fichier par défaut existe s'il a été supprimé
        plugin.reloadConfig();
        loadConfig(); // Re-parse les valeurs
    }
}
