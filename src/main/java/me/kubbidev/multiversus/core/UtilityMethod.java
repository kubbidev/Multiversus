package me.kubbidev.multiversus.core;

import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.Random;

/**
 * A utility class providing various static methods.
 */
public class UtilityMethod {
    private UtilityMethod() {
    }

    /**
     * The random number generator used by the utility methods.
     */
    private static final Random random = new Random();

    /**
     * Checks if an item stack is air or null.
     *
     * @param item The item stack to check. Can be null.
     * @return true if the item stack is air or null, false otherwise.
     */
    public static boolean isAir(@Nullable ItemStack item) {
        return item == null || item.getType().isAir();
    }

    /**
     * Checks if an item stack is considered a weapon based on its durability
     * (Purely arbitrary but works decently).
     *
     * @param item The item stack to check. Can be null.
     * @return true if the item stack is a weapon, false otherwise.
     */
    public static boolean isWeapon(@Nullable ItemStack item) {
        return item != null && item.getType().getMaxDurability() > 0;
    }
}