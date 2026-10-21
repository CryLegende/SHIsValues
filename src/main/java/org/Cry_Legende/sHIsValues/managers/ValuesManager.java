package org.Cry_Legende.sHIsValues.managers;

import org.Cry_Legende.sHIsValues.SHIsValues;
import org.Cry_Legende.sHIsValues.utils.BlockValue;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ValuesManager {

    private final SHIsValues plugin;
    private final List<BlockValue> blockValues = new ArrayList<>();

    public ValuesManager(SHIsValues plugin) {
        this.plugin = plugin;
    }

    /**
     * Détermine si un matériau peut être affiché dans un inventaire en tant qu'item.
     * Cette méthode utilise la vérification intégrée à Bukkit, qui est la plus fiable.
     *
     * @param material Le matériau à vérifier.
     * @return true si le matériau a une représentation en item, false sinon.
     */
    private boolean isMaterialRenderable(Material material) {
        if (material == null || material.isAir()) {
            return false;
        }

        // La méthode isItem() vérifie si le matériau peut exister en tant qu'ItemStack.
        // Elle gère automatiquement les cas comme FIRE, WATER (le bloc), LAVA (le bloc),
        // les têtes murales (ZOMBIE_WALL_HEAD), les pistons en mouvement, etc.
        // C'est la solution parfaite pour éviter une liste manuelle.
        return material.isItem();
    }

    public boolean loadBlockValues() {
        blockValues.clear();

        File serverRootFolder = plugin.getDataFolder().getParentFile().getParentFile();
        File ssb2ValuesFile = new File(serverRootFolder, plugin.getConfigManager().ssb2ValuesPath);

        if (!ssb2ValuesFile.exists()) {
            plugin.getLogger().severe("Le fichier " + ssb2ValuesFile.getAbsolutePath() + " n'a pas été trouvé !");
            return false;
        }

        FileConfiguration ssb2Config = YamlConfiguration.loadConfiguration(ssb2ValuesFile);

        for (String key : ssb2Config.getKeys(false)) {
            Material material = Material.matchMaterial(key.toUpperCase());
            if (material == null) {
                plugin.getLogger().warning("Matériau inconnu dans " + ssb2ValuesFile.getName() + ": " + key);
                continue;
            }

            // --- C'EST LA CORRECTION PRINCIPALE ---
            // On vérifie ici si le matériau peut être affiché avant même de le traiter.
            if (!isMaterialRenderable(material)) {
                // On logue (optionnel) pour savoir quels blocs sont ignorés.
                plugin.getLogger().info("Matériau '" + material.name() + "' ignoré car il n'a pas de représentation en item.");
                continue; // On passe au bloc suivant.
            }

            double value = ssb2Config.getDouble(key, 0.0);

            if (value > 0) {
                blockValues.add(new BlockValue(material, value));
            }
        }

        if (blockValues.isEmpty() && !ssb2Config.getKeys(false).isEmpty()) {
            plugin.getLogger().warning("Aucune valeur de bloc affichable (valeur > 0 et item valide) n'a été chargée depuis " + ssb2ValuesFile.getName() +
                ", mais le fichier contient des entrées. Vérifiez les valeurs et les types de matériaux.");
        } else if (ssb2Config.getKeys(false).isEmpty()) {
            plugin.getLogger().warning("Le fichier " + ssb2ValuesFile.getName() + " est vide ou ne contient aucune clé de bloc.");
        }

        Collections.sort(blockValues);
        plugin.getLogger().info("Chargé " + blockValues.size() + " valeurs de blocs affichables depuis " + ssb2ValuesFile.getName());
        return true;
    }

    public List<BlockValue> getBlockValues() {
        return Collections.unmodifiableList(blockValues);
    }

    public BlockValue getBlockValue(Material material) {
        for (BlockValue bv : blockValues) {
            if (bv.getMaterial() == material) {
                return bv;
            }
        }
        return null;
    }

    public int getPageForItem(Material material, int itemsPerPage) {
        if (itemsPerPage <= 0) return 1;

        List<BlockValue> currentValues = getBlockValues();
        for (int i = 0; i < currentValues.size(); i++) {
            if (currentValues.get(i).getMaterial() == material) {
                return (i / itemsPerPage) + 1;
            }
        }
        return -1;
    }
}
