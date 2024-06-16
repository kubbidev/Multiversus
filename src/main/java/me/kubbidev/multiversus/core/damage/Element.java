package me.kubbidev.multiversus.core.damage;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

@SuppressWarnings("UnnecessaryUnicodeEscape")
public enum Element {
    FIRE     (Component.text("\uD83D\uDD25", NamedTextColor.RED)),
    ICE      (Component.text("\u2744",       NamedTextColor.AQUA)),
    EARTH    (Component.text("\u20AA",       NamedTextColor.DARK_GREEN)),
    WIND     (Component.text("\uD83C\uDF0A", NamedTextColor.GRAY)),
    THUNDER  (Component.text("\u2605",       NamedTextColor.YELLOW)),
    WATER    (Component.text("\uD83C\uDF0A", NamedTextColor.DARK_AQUA)),
    DARKNESS (Component.text("\u263D",       NamedTextColor.DARK_GRAY)),
    LIGHTNESS(Component.text("\u2600",       NamedTextColor.WHITE));

    private final Component icon;

    Element(Component icon) {
        this.icon = icon;
    }

    public Component getIcon() {
        return this.icon;
    }
}