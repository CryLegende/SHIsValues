package org.Cry_Legende.sHIsValues.utils; // Mettre dans un package utils par exemple

import org.bukkit.Material;

public class BlockValue implements Comparable<BlockValue> {
    private final Material material;
    private final double value;

    public BlockValue(Material material, double value) {
        this.material = material;
        this.value = value;
    }

    public Material getMaterial() {
        return material;
    }

    public double getValue() {
        return value;
    }

    @Override
    public int compareTo(BlockValue other) {
        // Tri décroissant par valeur
        return Double.compare(other.value, this.value);
    }

    @Override
    public String toString() {
        return "BlockValue{" +
            "material=" + material +
            ", value=" + value +
            '}';
    }
}
