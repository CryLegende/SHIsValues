package org.Cry_Legende.sHIsValues.access;

import com.bgsoftware.superiorskyblock.api.SuperiorSkyblockAPI;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.island.PermissionNode;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.Cry_Legende.sHIsValues.SHIsValues;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * Placeholders d'accès d'un joueur (le joueur du contexte) à l'île d'un autre joueur ({@code <chef>} = un membre de
 * l'île visée, en pratique celui qui regarde : {@code %viewer%} dans /infos) :
 * <ul>
 *   <li>{@code %shisvalues_acces_status_<chef>%} : Chef, Membre (rôle), Coopérateur, Banni ou Visiteur</li>
 *   <li>{@code %shisvalues_acces_allowed_<chef>%} : permissions particulières accordées (au plus 4 noms puis +N)</li>
 *   <li>{@code %shisvalues_acces_denied_<chef>%} : permissions particulières retirées</li>
 * </ul>
 * Textes dans config.yml › access-placeholders.
 */
public final class AccessExpansion extends PlaceholderExpansion {

    private static final int MAX_NAMES = 4;

    private final SHIsValues plugin;
    private final AccessList access;

    public AccessExpansion(SHIsValues plugin, AccessList access) {
        this.plugin = plugin;
        this.access = access;
    }

    @Override
    public @NotNull String getIdentifier() {
        return "shisvalues";
    }

    @Override
    public @NotNull String getAuthor() {
        return "Cry_Legende";
    }

    @Override
    public @NotNull String getVersion() {
        return plugin.getDescription().getVersion();
    }

    @Override
    public boolean persist() {
        return true;
    }

    private String text(String key, String def) {
        return plugin.getConfig().getString("access-placeholders." + key, def);
    }

    @Override
    public String onRequest(OfflinePlayer player, @NotNull String params) {
        if (player == null || !params.startsWith("acces_")) {
            return null;
        }
        String rest = params.substring("acces_".length());
        int sep = rest.indexOf('_');
        if (sep <= 0 || sep == rest.length() - 1) {
            return null;
        }
        String kind = rest.substring(0, sep);
        String ownerName = rest.substring(sep + 1);

        SuperiorPlayer owner = SuperiorSkyblockAPI.getPlayer(ownerName);
        Island island = owner == null ? null : owner.getIsland();
        if (island == null) {
            return text("no-island", "&7Aucune île");
        }
        SuperiorPlayer target = SuperiorSkyblockAPI.getPlayer(player.getUniqueId());
        if (target == null) {
            return "";
        }
        return switch (kind) {
            case "status" -> status(island, target);
            case "allowed" -> list(island, target, true);
            case "denied" -> list(island, target, false);
            default -> null;
        };
    }

    private String status(Island island, SuperiorPlayer target) {
        if (island.getOwner() != null && island.getOwner().getUniqueId().equals(target.getUniqueId())) {
            return text("owner", "&6Chef de l'île");
        }
        if (island.isMember(target)) {
            return text("member", "&aMembre &7(%role%&7)").replace("%role%", target.getPlayerRole().getDisplayName());
        }
        if (island.isCoop(target)) {
            return text("coop", "&bCoopérateur");
        }
        if (island.isBanned(target)) {
            return text("banned", "&cBanni");
        }
        return text("visitor", "&7Visiteur");
    }

    private String list(Island island, SuperiorPlayer target, boolean allowed) {
        PermissionNode node = island.getPlayerPermissions().get(target);
        List<String> names = new ArrayList<>();
        if (node != null) {
            node.getCustomPermissions().forEach((privilege, value) -> {
                if (Boolean.TRUE.equals(value) == allowed) {
                    names.add(access.privilegeName(privilege));
                }
            });
        }
        if (names.isEmpty()) {
            return text("none", "&8aucune");
        }
        names.sort(String.CASE_INSENSITIVE_ORDER);
        String color = allowed ? "&a" : "&c";
        String shown = String.join("&7, " + color, names.subList(0, Math.min(MAX_NAMES, names.size())));
        return color + shown + (names.size() > MAX_NAMES ? " &7+" + (names.size() - MAX_NAMES) : "");
    }
}
