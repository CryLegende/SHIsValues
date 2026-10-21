package org.Cry_Legende.sHIsValues; // Assure-toi que c'est ton nom de package exact

import org.Cry_Legende.sHIsValues.commands.ValuesCommand; // Pour /shiv
import org.Cry_Legende.sHIsValues.listeners.IslandSubcommandListener; // Pour /is values et /is value
import org.Cry_Legende.sHIsValues.managers.ConfigManager;
import org.Cry_Legende.sHIsValues.managers.ValuesManager;
// ValuesGUI est utilisé par IslandSubcommandListener via le getter, donc pas besoin d'import direct ici si non utilisé ailleurs
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.plugin.java.JavaPlugin;

public class SHIsValues extends JavaPlugin {

    private static SHIsValues instance;
    private ConfigManager configManager;
    private ValuesManager valuesManager;
    private ValuesGUI valuesGUI; // Doit être initialisé
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
        configManager = new ConfigManager(this);
        configManager.loadConfig(); // Charge les valeurs depuis config.yml

        // Gestionnaire des valeurs des blocs
        valuesManager = new ValuesManager(this);
        if (!valuesManager.loadBlockValues()) {
            // Le message d'erreur est déjà loggué dans ValuesManager
            getLogger().severe("Échec du chargement des valeurs de blocs. Le plugin pourrait ne pas fonctionner comme prévu.");
            // Tu pourrais choisir de désactiver le plugin ici si c'est critique:
            // getServer().getPluginManager().disablePlugin(this);
            // return;
        }

        // Initialisation du gestionnaire de GUI
        valuesGUI = new ValuesGUI(this); // Important: initialiser avant de l'utiliser ou de le passer

        // Enregistrement de la commande principale /shiv
        ValuesCommand shivExecutor = new ValuesCommand(this); // L'exécuteur pour /shiv
        if (getCommand("shiv") != null) {
            getCommand("shiv").setExecutor(shivExecutor);
            getCommand("shiv").setTabCompleter(shivExecutor);
        } else {
            getLogger().severe("La commande 'shiv' n'a pas pu être trouvée/enregistrée. Vérifiez plugin.yml.");
        }


        // Enregistrement des Listeners
        getServer().getPluginManager().registerEvents(valuesGUI, this); // Pour les clics dans le GUI de /shiv
        getServer().getPluginManager().registerEvents(new IslandSubcommandListener(this), this); // Pour intercepter /is values et /is value

        getLogger().info("SHIsValues a été activé !");
        getLogger().info("Commandes disponibles: /shiv, et potentiellement /is values, /is value (si configuré et permissions accordées).");
    }

    @Override
    public void onDisable() {
        // Logique de désactivation si nécessaire (ex: sauvegarder des données, fermer des connexions)
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

    public ValuesGUI getValuesGUI() { // Nécessaire pour IslandSubcommandListener
        return valuesGUI;
    }

    public boolean isPlaceholderApiHooked() {
        return placeholderApiHooked;
    }

    // Méthode utilitaire pour colorer les messages
    public String color(String message) {
        if (message == null) return "";
        return ChatColor.translateAlternateColorCodes('&', message);
    }
}