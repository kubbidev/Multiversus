package me.kubbidev.multiversus.core;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.metadata.Metadatable;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.stream.Collectors;

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
     * Checks if an entity is vanished based on its metadata.
     *
     * @param entity The entity to check.
     * @return true if the entity is vanished, false otherwise.
     */
    public static boolean isVanished(Metadatable entity) {
        return entity.getMetadata("vanished").stream().anyMatch(MetadataValue::asBoolean);
    }

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

    /**
     * Converts a camel case string (e.g., "MySimpleWord") to kebab-case (e.g., "my_simple_word").
     *
     * @param input the camel case string to be converted
     * @return the converted kebab-case string, or the input string if it is null or empty
     */
    public static String convertToKebabCase(String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }

        StringBuilder result = new StringBuilder();
        char[] chars = input.toCharArray();

        for (char c : chars) {
            if (Character.isUpperCase(c)) {
                if (result.length() != 0) {
                    result.append('_');
                }
                result.append(Character.toLowerCase(c));
            } else {
                result.append(c);
            }
        }
        return result.toString();
    }

    /**
     * Super useful to display enum names like DIAMOND_SWORD in chat.
     *
     * @param input String with lower cases and spaces only
     * @return Same string with capital letters at the beginning of each word.
     */
    public static String caseOnWords(String input) {
        StringBuilder builder = new StringBuilder(input);

        boolean isLastSpace = true;
        for (int i = 0; i < builder.length(); i++) {
            char ch = builder.charAt(i);
            if (isLastSpace && ch >= 'a' && ch <= 'z') {
                builder.setCharAt(i, (char) (ch + ('A' - 'a')));
                isLastSpace = false;
            } else {
                isLastSpace = (ch == ' ');
            }
        }
        return builder.toString();
    }

    /**
     * Reads an icon string and converts it into an {@link ItemStack}.
     * <p>
     * The icon string should be in the format "MATERIAL" or "MATERIAL:customModelData".
     * <p>
     * Example formats:
     * <ul>
     * <li>{@code DIAMOND}</li>
     * <li>{@code DIAMOND:123}</li>
     * </ul>
     *
     * @param icon The icon string representing the material and optional custom model data.
     * @return The created {@link ItemStack}.
     * @throws IllegalArgumentException If the material is invalid or the custom model data is not a number.
     */
    @SuppressWarnings("CodeBlock2Expr")
    public static ItemStack readIcon(String icon) throws IllegalArgumentException {
        String[] split = icon.split(":");
        Material material = Material.valueOf(split[0].toUpperCase(Locale.ROOT)
                .replace("-", "_")
                .replace(" ", "_"));

        ItemStack itemStack = new ItemStack(material);
        if (split.length > 1) {
            itemStack.editMeta(m -> {
                m.setCustomModelData(Integer.parseInt(split[1]));
            });
        }
        return itemStack;
    }

    /**
     * Deserializes a string into a {@link Component} using MiniMessage.
     *
     * @param input The input string to deserialize.
     * @return The deserialized {@link Component}.
     */
    public static Component deserialize(String input) {
        return MiniMessage.miniMessage().deserialize(input);
    }

    /**
     * Deserializes a list of strings into a list of {@link Component} objects using MiniMessage.
     *
     * @param input The list of input strings to deserialize.
     * @return The list of deserialized {@link Component} objects.
     */
    public static List<Component> deserialize(List<String> input) {
        return input.stream().map(UtilityMethod::deserialize).collect(Collectors.toList());
    }
}
