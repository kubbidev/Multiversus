package me.kubbidev.multiversus.core.modifier.instance;

import me.kubbidev.multiversus.config.MultiConfiguration;
import me.kubbidev.multiversus.core.modifier.EntityModifier;
import me.kubbidev.multiversus.core.modifier.ModifierSource;
import me.kubbidev.multiversus.core.modifier.ModifierType;
import me.kubbidev.multiversus.core.util.EquipmentSlot;

import java.util.UUID;

/**
 * Used anywhere where instances similar to {@link org.bukkit.attribute.Attribute}
 * instances are being modified by numerical modifiers.
 */
public abstract class InstanceModifier extends EntityModifier {

    protected final double value;
    protected final ModifierType type;

    public InstanceModifier(String key, double value) {
        this(ModifierSource.OTHER, EquipmentSlot.OTHER, key, value, ModifierType.FLAT);
    }

    public InstanceModifier(ModifierSource source, EquipmentSlot slot, String key, double value, ModifierType type) {
        this(UUID.randomUUID(), source, slot, key, value, type);
    }

    public InstanceModifier(UUID uniqueId, ModifierSource source, EquipmentSlot slot, String key, double value, ModifierType type) {
        super(uniqueId, source, slot, key);
        this.value = value;
        this.type = type;
    }

    public double getValue() {
        return this.value;
    }

    public ModifierType getType() {
        return this.type;
    }

    public String toString(MultiConfiguration configuration) {
        return configuration.getDecimalFormat().format(this.value) + (this.type == ModifierType.RELATIVE ? '%' : "");
    }
}