package org.Cry_Legende.sHIsValues.access;

import com.bgsoftware.superiorskyblock.api.SuperiorSkyblockAPI;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.island.IslandPrivilege;
import com.bgsoftware.superiorskyblock.api.island.PermissionNode;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.Cry_Legende.sHIsValues.SHIsValues;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * {@code /is acces} : liste des joueurs qui ont des permissions particulières sur l'île du joueur (réglées
 * avec {@code /is permissions <joueur>}), avec les permissions accordées et retirées. Un clic sur une ligne
 * ouvre le menu de permissions de ce joueur. Les noms des permissions viennent du menu de SuperiorSkyblock2
 * ({@code menus/permissions.yml}), sinon de leur identifiant.
 */
public final class AccessList {

    private static final LegacyComponentSerializer LEGACY = LegacyComponentSerializer.legacyAmpersand();

    private final SHIsValues plugin;
    private final Map<String, String> privilegeNames = new HashMap<>();

    public AccessList(SHIsValues plugin) {
        this.plugin = plugin;
        loadPrivilegeNames();
    }

    /** Noms affichés des permissions, lus dans le menu de permissions de SuperiorSkyblock2. */
    public void loadPrivilegeNames() {
        privilegeNames.clear();
        File file = new File(plugin.getDataFolder().getParentFile(), "SuperiorSkyblock2/menus/permissions.yml");
        if (!file.isFile()) {
            return;
        }
        ConfigurationSection section = YamlConfiguration.loadConfiguration(file).getConfigurationSection("permissions");
        if (section == null) {
            return;
        }
        for (String key : section.getKeys(false)) {
            String name = section.getString(key + ".permission-enabled.name");
            if (name != null) {
                String plain = ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&', name)).trim();
                if (!plain.isEmpty()) {
                    privilegeNames.put(key.toLowerCase(Locale.ROOT), plain);
                }
            }
        }
    }

    private String message(String key, String def) {
        FileConfiguration config = plugin.getConfig();
        return config.getString("access-messages." + key, def);
    }

    /** Nom affiché d'une permission (menu de permissions de SSB2, sinon son identifiant). */
    public String privilegeName(IslandPrivilege privilege) {
        String id = privilege.getName().toLowerCase(Locale.ROOT);
        return privilegeNames.getOrDefault(id, id.replace('_', ' '));
    }

    public void show(Player player) {
        SuperiorPlayer superiorPlayer = SuperiorSkyblockAPI.getPlayer(player);
        Island island = superiorPlayer == null ? null : superiorPlayer.getIsland();
        if (island == null) {
            player.sendMessage(LEGACY.deserialize(message("no-island",
                    "&7&l« &c&lSH &8&l» &7Vous n'avez pas d'île (&c/is&7).")));
            return;
        }

        List<Map.Entry<SuperiorPlayer, PermissionNode>> rows = new ArrayList<>();
        for (Map.Entry<SuperiorPlayer, PermissionNode> entry : island.getPlayerPermissions().entrySet()) {
            if (entry.getKey() != null && entry.getValue() != null && !entry.getValue().getCustomPermissions().isEmpty()) {
                rows.add(entry);
            }
        }
        if (rows.isEmpty()) {
            player.sendMessage(LEGACY.deserialize(message("empty",
                    "&7&l« &e&lSH &8&l» &7Aucun joueur n'a de permission particulière sur votre île.")));
            return;
        }
        rows.sort(Comparator.comparing(entry -> entry.getKey().getName(), String.CASE_INSENSITIVE_ORDER));

        player.sendMessage(LEGACY.deserialize(message("header",
                "&7&l« &e&lSH &8&l» &7Permissions particulières sur votre île (&e%count% joueur(s)&7) :")
                .replace("%count%", String.valueOf(rows.size()))));

        String allowedFormat = message("allowed", "&a✔ %list%");
        String deniedFormat = message("denied", "&c✘ %list%");
        String lineFormat = message("line", "&8• &e%player% &8» %permissions%");
        String hover = message("hover", "&7Cliquez pour modifier les permissions de &e%player%");
        for (Map.Entry<SuperiorPlayer, PermissionNode> row : rows) {
            List<String> allowed = new ArrayList<>();
            List<String> denied = new ArrayList<>();
            row.getValue().getCustomPermissions().forEach((privilege, value) ->
                    (Boolean.TRUE.equals(value) ? allowed : denied).add(privilegeName(privilege)));
            allowed.sort(String.CASE_INSENSITIVE_ORDER);
            denied.sort(String.CASE_INSENSITIVE_ORDER);
            List<String> parts = new ArrayList<>();
            if (!allowed.isEmpty()) {
                parts.add(allowedFormat.replace("%list%", String.join("&7, &a", allowed)));
            }
            if (!denied.isEmpty()) {
                parts.add(deniedFormat.replace("%list%", String.join("&7, &c", denied)));
            }
            String name = row.getKey().getName();
            Component line = LEGACY.deserialize(lineFormat
                            .replace("%player%", name)
                            .replace("%permissions%", String.join(" &8| ", parts)))
                    .clickEvent(ClickEvent.runCommand("/is permissions " + name))
                    .hoverEvent(HoverEvent.showText(LEGACY.deserialize(hover.replace("%player%", name))));
            player.sendMessage(line);
        }
    }
}
