package me.kubbidev.multiversus.core.metadata;

import com.google.common.reflect.TypeToken;
import me.kubbidev.multiversus.core.metadata.cooldown.CooldownMap;
import me.kubbidev.multiversus.core.modifier.skill.SkillModifierMap;
import me.kubbidev.multiversus.core.skill.handler.SkillHandler;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;

public final class MetadataProvider {
    private MetadataProvider() {
    }

    /**
     * Metadata key used to retrieve {@link LivingEntity} cooldown map from memory.
     */
    public static final MetadataKey<CooldownMap<SkillHandler<?>>> COOLDOWN_MAP = MetadataKey.create("cooldown_map",
            new TypeToken<CooldownMap<SkillHandler<?>>>() {});

    /**
     * Gets the provided {@link LivingEntity}'s cooldown map associated to him.
     *
     * @param entity The entity owning the map.
     * @return cooldown map or new instance if not found
     */
    public static CooldownMap<SkillHandler<?>> getCooldownMap(Entity entity) {
        MetadataMap metadataMap = Metadata.provide(entity);
        return metadataMap.getOrPut(COOLDOWN_MAP, CooldownMap::create);
    }

    /**
     * Metadata key used to retrieve {@link LivingEntity} skill modifiers map from memory.
     */
    public static final MetadataKey<SkillModifierMap> SKILL_MODIFIER_MAP = MetadataKey.create("skill_modifier_map", SkillModifierMap.class);

    /**
     * Gets the provided {@link LivingEntity}'s skill modifier map associated to him.
     *
     * @param entity The entity owning the map.
     * @return skill modifier map or new instance if not found
     */
    public static SkillModifierMap getModifierMap(Entity entity) {
        MetadataMap metadataMap = Metadata.provide(entity);
        return metadataMap.getOrPut(SKILL_MODIFIER_MAP, SkillModifierMap::new);
    }
}