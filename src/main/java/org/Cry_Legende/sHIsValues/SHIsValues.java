package org.Cry_Legende.sHIsValues;

import org.Cry_Legende.sHIsValues.commands.ValuesCommand;
import org.Cry_Legende.sHIsValues.listeners.IslandSubcommandListener;
import org.Cry_Legende.sHIsValues.managers.ConfigManager;
import org.Cry_Legende.sHIsValues.managers.ValuesManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public class SHIsValues extends JavaPlugin {

    private static SHIsValues instance;
    private ConfigManager configManager;
    private ValuesManager valuesManager;
    private ValuesGUI valuesGUI;
    private org.Cry_Legende.sHIsValues.access.AccessList accessList;
    private FileConfiguration settings;
    private FileConfiguration messages;

    /** Erreur de lecture d'un fichier de configuration : nom du fichier et cause. */
    public static final class ConfigException extends Exception {
        private final String file;

        ConfigException(String file, String message) {
            super(message);
            this.file = file;
        }

        public String getFile() {
            return file;
        }
    }

    public org.Cry_Legende.sHIsValues.access.AccessList getAccessList() {
        return accessList;
    }
    private boolean placeholderApiHooked = false;

    @Override
    public void onEnable() {
        instance = this;

        // Vérifier PlaceholderAPI
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            placeholderApiHooked = true;
            getLogger().info("PlaceholderAPI trouvé et activé pour SHIsValues !");
        } else {
            getLogger().info("PlaceholderAPI non trouvé. Les placeholders PAPI ne seront pas fonctionnels.");
        }

        // Configuration
        saveDefaultConfig(); // Crée config.yml s'il n'existe pas
        saveMessagesIfAbsent();
        configManager = new ConfigManager(this);
        try {
            configManager.loadConfig();
        } catch (ConfigException e) {
            getLogger().severe("Fichier " + e.getFile() + " illisible, valeurs du jar utilisées : " + e.getMessage());
            settings = bundled("config.yml");
            messages = bundled("messages.yml");
            configManager.parse();
        }

        // Gestionnaire des valeurs des blocs
        valuesManager = new ValuesManager(this);
        if (!valuesManager.loadBlockValues()) {
            // Le message d'erreur est déjà loggué dans ValuesManager
            getLogger().severe("Échec du chargement des valeurs de blocs. Le plugin pourrait ne pas fonctionner comme prévu.");
        }

        // Initialisation du gestionnaire de GUI
        valuesGUI = new ValuesGUI(this);
        // /is acces : joueurs ayant des permissions particulières sur l'île
        accessList = new org.Cry_Legende.sHIsValues.access.AccessList(this);
        if (placeholderApiHooked) {
            // %shisvalues_acces_<status|allowed|denied>_<chef>% (utilisé par /infos <joueur>)
            new org.Cry_Legende.sHIsValues.access.AccessExpansion(this, accessList).register();
        }

        // Enregistrement de la commande /shisvalues (alias /shiv)
        ValuesCommand executor = new ValuesCommand(this);
        if (getCommand("shisvalues") != null) {
            getCommand("shisvalues").setExecutor(executor);
            getCommand("shisvalues").setTabCompleter(executor);
        } else {
            getLogger().severe("La commande 'shisvalues' n'a pas pu être trouvée/enregistrée. Vérifiez plugin.yml.");
        }

        // Enregistrement des Listeners
        getServer().getPluginManager().registerEvents(valuesGUI, this); // Pour les clics dans le GUI
        getServer().getPluginManager().registerEvents(new IslandSubcommandListener(this), this); // Pour intercepter /is values, /is value et /is acces

        getLogger().info("SHIsValues a été activé !");
        getLogger().info("Commande disponible: /shisvalues, et les sous-commandes /is values, /is value, /is acces pour les joueurs qui en ont le droit.");
    }

    @Override
    public void onDisable() {
        getLogger().info("SHIsValues a été désactivé.");
    }

    // Getters pour que d'autres classes puissent accéder à ces instances
    public static SHIsValues getInstance() {
        return instance;
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public ValuesManager getValuesManager() {
        return valuesManager;
    }

    public ValuesGUI getValuesGUI() {
        return valuesGUI;
    }

    public boolean isPlaceholderApiHooked() {
        return placeholderApiHooked;
    }

    /** config.yml (réglages du menu), tel que lu par le dernier chargement réussi. */
    @Override
    public FileConfiguration getConfig() {
        return settings != null ? settings : super.getConfig();
    }

    /** messages.yml, tel que lu par le dernier chargement réussi. */
    public FileConfiguration getMessages() {
        return messages;
    }

    /**
     * Lit config.yml et messages.yml sans rien remplacer : si l'un des deux est invalide, l'ancienne configuration
     * reste en place. Les fichiers lus sont appliqués par {@link #applyFiles}.
     */
    public FileConfiguration[] readFiles() throws ConfigException {
        return new FileConfiguration[]{readYaml("config.yml"), readYaml("messages.yml")};
    }

    public void applyFiles(FileConfiguration[] files) {
        settings = files[0];
        messages = files[1];
    }

    private YamlConfiguration readYaml(String name) throws ConfigException {
        YamlConfiguration yaml = new YamlConfiguration();
        try {
            yaml.load(new File(getDataFolder(), name));
        } catch (IOException | InvalidConfigurationException e) {
            String reason = e.getMessage() == null ? e.toString() : e.getMessage().lines().findFirst().orElse("");
            throw new ConfigException(name, reason);
        }
        yaml.setDefaults(bundled(name));
        return yaml;
    }

    private YamlConfiguration bundled(String name) {
        try (InputStream in = getResource(name)) {
            return in == null ? new YamlConfiguration()
                : YamlConfiguration.loadConfiguration(new InputStreamReader(in, StandardCharsets.UTF_8));
        } catch (IOException e) {
            return new YamlConfiguration();
        }
    }

    /**
     * Premier démarrage avec ce fichier : messages.yml est créé à partir des sections messages, access-messages et
     * access-placeholders de config.yml (textes déjà en place), complétées par les textes du jar. Ces sections de
     * config.yml ne sont plus lues ensuite.
     */
    private void saveMessagesIfAbsent() {
        File file = new File(getDataFolder(), "messages.yml");
        if (file.exists()) {
            return;
        }
        saveResource("messages.yml", false);
        YamlConfiguration old = YamlConfiguration.loadConfiguration(new File(getDataFolder(), "config.yml"));
        YamlConfiguration created = YamlConfiguration.loadConfiguration(file);
        ConfigurationSection flat = old.getConfigurationSection("messages");
        if (flat != null) {
            for (String key : flat.getKeys(false)) {
                if (flat.isString(key)) {
                    created.set(key, flat.getString(key));
                }
            }
        }
        for (String section : new String[]{"access-messages", "access-placeholders"}) {
            ConfigurationSection source = old.getConfigurationSection(section);
            if (source != null) {
                for (String key : source.getKeys(false)) {
                    if (source.isString(key)) {
                        created.set(section + "." + key, source.getString(key));
                    }
                }
            }
        }
        try {
            created.save(file);
            getLogger().info("Messages repris de config.yml dans messages.yml (ces sections de config.yml ne servent plus).");
        } catch (IOException e) {
            getLogger().warning("messages.yml non écrit : " + e.getMessage());
        }
    }

    // Méthode utilitaire pour colorer les messages
    public String color(String message) {
        if (message == null) return "";
        return ChatColor.translateAlternateColorCodes('&', message);
    }
}
