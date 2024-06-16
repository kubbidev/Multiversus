package me.kubbidev.multiversus.core.damage;

import org.jetbrains.annotations.Nullable;

import java.util.*;

public class DamageMetadata implements Cloneable {
    private final List<DamagePacket> packets = new ArrayList<>();

    /**
     * The first damage packet to be registered inside of this damage
     * metadata. It is usually the most significant (highest value)
     * or at least the base damage on which all modifiers are then
     * applied.
     * <p>
     * This field is a direct reference of an existing element
     * of the collection returned by {@link #getPackets()}.
     * <p>
     * Although not common, it can be null.
     */
    @Nullable
    private final DamagePacket initialPacket;

    private boolean weaponCrit;
    private boolean skillCrit;

    private final Set<Element> elementalCrit = new HashSet<>();

    /**
     * Used to register an attack with NO initial packet.
     */
    public DamageMetadata() {
        this.initialPacket = null;
    }

    /**
     * Used to register an attack.
     *
     * @param damage The attack damage
     * @param types  The attack damage types
     */
    public DamageMetadata(double damage, DamageType... types) {
        this(damage, null, types);
    }

    /**
     * Used to register an attack.
     *
     * @param damage  The attack damage
     * @param element If this is an elemental attack
     * @param types   The attack damage types
     */
    public DamageMetadata(double damage, @Nullable Element element, DamageType... types) {
        this.initialPacket = new DamagePacket(damage, element, types);
        this.packets.add(this.initialPacket);
    }

    public List<DamagePacket> getPackets() {
        return this.packets;
    }

    public @Nullable DamagePacket getInitialPacket() {
        return this.initialPacket;
    }

    public boolean isWeaponCrit() {
        return this.weaponCrit;
    }

    public void setWeaponCrit(boolean weaponCrit) {
        this.weaponCrit = weaponCrit;
    }

    public boolean isSkillCrit() {
        return this.skillCrit;
    }

    public void setSkillCrit(boolean skillCrit) {
        this.skillCrit = skillCrit;
    }

    public boolean isElementCrit(Element element) {
        return this.elementalCrit.contains(element);
    }

    public void registerElementalCrit(Element element) {
        this.elementalCrit.add(element);
    }

    /**
     * You cannot deal less than 0.01 damage.
     * <p>
     * This is an arbitrary positive constant, as Multiversus and other plugins consider
     * 0-damage events to be fake damage events used to check for the PvP/PvE flag.
     */
    public static final double MINIMAL_DAMAGE = 0.01;

    public double getDamage() {
        double d = 0;

        for (DamagePacket packet : this.packets) {
            d += packet.getFinalValue();
        }
        return Math.max(MINIMAL_DAMAGE, d);
    }

    /**
     * @param element If null, non-elemental damage will be returned.
     */
    public double getDamage(@Nullable Element element) {
        double d = 0;

        for (DamagePacket packet : this.packets) {
            if (Objects.equals(packet.getElement(), element))
                d += packet.getFinalValue();
        }
        return d;
    }

    public double getDamage(DamageType type) {
        double d = 0;

        for (DamagePacket packet : this.packets) {
            if (packet.hasType(type))
                d += packet.getFinalValue();
        }
        return d;
    }

    public Map<Element, Double> mapElementalDamage() {
        Map<Element, Double> mapped = new HashMap<>();

        for (DamagePacket packet : this.packets) {
            if (packet.getElement() != null)
                mapped.put(packet.getElement(), mapped.getOrDefault(packet.getElement(), 0d) + packet.getFinalValue());
        }
        return mapped;
    }

    /**
     * @return Set containing all the damage types found
     * in all the different damage packets.
     */
    public Set<DamageType> collectTypes() {
        Set<DamageType> collected = new HashSet<>();

        for (DamagePacket packet : this.packets) {
            Collections.addAll(collected, packet.getTypes());
        }
        return collected;
    }

    /**
     * @return Set containing all the elements found
     * in all the different damage packets.
     */
    public Set<Element> collectElements() {
        Set<Element> collected = new HashSet<>();

        for (DamagePacket packet : this.packets) {
            if (packet.getElement() != null)
                collected.add(packet.getElement());
        }
        return collected;
    }

    /**
     * @return Iterates through all registered damage packets and
     * see if any has this damage type.
     */
    public boolean hasType(DamageType type) {
        for (DamagePacket packet : this.packets) {
            if (packet.hasType(type))
                return true;
        }
        return false;
    }

    /**
     * @param element If null, will return true if it has non-elemental damage.
     * @return Iterates through all registered damage packets and
     * see if any has this element.
     */
    public boolean hasElement(@Nullable Element element) {
        for (DamagePacket packet : this.packets) {
            if (Objects.equals(packet.getElement(), element))
                return true;
        }
        return false;
    }

    /**
     * Registers a new damage packet.
     *
     * @param value Damage dealt by another source, this could be an on-hit
     *              skill increasing the damage of the current attack.
     * @param types The damage types of the packet being registered
     * @return The same modified damage metadata
     */
    public DamageMetadata add(double value, DamageType... types) {
        this.packets.add(new DamagePacket(value, types));
        return this;
    }

    /**
     * Registers a new elemental damage packet.
     *
     * @param value   Damage dealt by another source, this could be an on-hit
     *                skill increasing the damage of the current attack.
     * @param element The element
     * @param types   The damage types of the packet being registered
     * @return The same modified damage metadata
     */
    public DamageMetadata add(double value, @Nullable Element element, DamageType... types) {
        this.packets.add(new DamagePacket(value, element, types));
        return this;
    }

    /**
     * Register a multiplicative damage modifier in all damage packets.
     * <p>
     * This is used for critical strikes which modifier should
     * NOT stack up with damage boosting statistics.
     *
     * @param coefficient Multiplicative coefficient. 1.5 will
     *                    increase final damage by 50%
     * @return The same damage metadata
     */
    public DamageMetadata multiplicativeModifier(double coefficient) {
        for (DamagePacket packet : this.packets) {
            packet.multiplicativeModifier(coefficient);
        }
        return this;
    }

    /**
     * Registers a multiplicative damage modifier
     * which applies to any damage packet
     *
     * @param multiplier From 0 to infinity, 1 increases damage by 100%.
     *                   This can be negative as well
     * @return The same damage metadata
     */
    public DamageMetadata additiveModifier(double multiplier) {
        for (DamagePacket packet : this.packets) {
            packet.additiveModifier(multiplier);
        }
        return this;
    }

    /**
     * Register a multiplicative damage modifier for a specific damage type.
     *
     * @param coefficient Multiplicative coefficient. 1.5 will
     *                    increase final damage by 50%
     * @param damageType  Specific damage type
     * @return The same damage metadata
     */
    public DamageMetadata multiplicativeModifier(double coefficient, DamageType damageType) {
        for (DamagePacket packet : this.packets) {
            if (packet.hasType(damageType))
                packet.multiplicativeModifier(coefficient);
        }
        return this;
    }

    /**
     * Register a multiplicative damage modifier for a specific element.
     *
     * @param coefficient Multiplicative coefficient. 1.5 will
     *                    increase final damage by 50%
     * @param element     If null, non-elemental damage will be considered
     * @return The same damage metadata
     */
    public DamageMetadata multiplicativeModifier(double coefficient, @Nullable Element element) {
        for (DamagePacket packet : this.packets) {
            if (Objects.equals(packet.getElement(), element))
                packet.multiplicativeModifier(coefficient);
        }
        return this;
    }

    /**
     * Registers a multiplicative damage modifier which only
     * applies to a specific damage type
     *
     * @param multiplier From 0 to infinity, 1 increases damage by 100%.
     *                   This can be negative as well
     * @param damageType Specific damage type
     * @return The same damage metadata
     */
    public DamageMetadata additiveModifier(double multiplier, DamageType damageType) {
        for (DamagePacket packet : this.packets) {
            if (packet.hasType(damageType))
                packet.additiveModifier(multiplier);
        }
        return this;
    }

    /**
     * Register an additive damage modifier for a specific element.
     *
     * @param coefficient Multiplicative coefficient. 1.5 will
     *                    increase final damage by 50%
     * @param element     If null, non-elemental damage will be considered
     * @return The same damage metadata
     */
    public DamageMetadata additiveModifier(double coefficient, Element element) {
        for (DamagePacket packet : this.packets) {
            if (Objects.equals(packet.getElement(), element))
                packet.additiveModifier(coefficient);
        }
        return this;
    }

    @SuppressWarnings("MethodDoesntCallSuperMethod")
    @Override
    public DamageMetadata clone() {
        DamageMetadata clone = new DamageMetadata();

        for (DamagePacket packet : this.packets) {
            clone.packets.add(packet.clone());
        }
        return clone;
    }
}