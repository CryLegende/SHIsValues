package org.Cry_Legende.sHIsValues.utils;

import org.bukkit.Material;

import java.util.List;

public class GuiItemConfig {
    private final String id; // L'identifiant de l'item (ex: "previous_button")
    private final boolean enabled;
    private final Material material;
    private final String name;
    private final List<String> lore;
    private final List<Integer> slots;
    private final String action; // ex: "PREVIOUS_PAGE", "NEXT_PAGE", null

    public GuiItemConfig(String id, boolean enabled, Material material, String name, List<String> lore, List<Integer> slots, String action) {
        this.id = id;
        this.enabled = enabled;
        this.material = material;
        this.name = name;
        this.lore = lore;
        this.slots = slots;
        this.action = action;
    }

    public String getId() {
        return id;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public Material getMaterial() {
        return material;
    }

    public String getName() {
        return name;
    }

    public List<String> getLore() {
        return lore;
    }

    public List<Integer> getSlots() {
        return slots;
    }

    public String getAction() {
        return action;
    }
}
