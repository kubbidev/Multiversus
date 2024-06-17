package me.kubbidev.multiversus.core.skill;

import me.kubbidev.multiversus.FBukkitPlugin;
import me.kubbidev.multiversus.core.UtilityMethod;
import me.kubbidev.multiversus.core.skill.handler.SkillHandler;
import me.kubbidev.multiversus.core.util.LinearValue;
import net.kyori.adventure.text.Component;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class RegisteredSkill {
    private final SkillHandler<?> handler;
    private final Map<String, LinearValue> defaultParameters = new HashMap<>();
    private final Map<String, DecimalFormat> decimalFormats = new HashMap<>();

    private final Component name;
    private final List<Component> lore;
    private final ItemStack icon;

    public RegisteredSkill(SkillHandler<?> handler, ConfigurationSection config) {
        this.handler = handler;

        this.name = UtilityMethod.deserialize(Objects.requireNonNull(config.getString("name"), "Could not find skill name"));
        this.lore = UtilityMethod.deserialize(Objects.requireNonNull(config.getStringList("lore"), "Could not find skill lore"));
        this.icon = UtilityMethod.readIcon(Objects.requireNonNull(config.getString("icon"), "Could not find skill icon"));

        for (String param : handler.getParameters()) {
            ConfigurationSection section = config.getConfigurationSection(param);
            if (section == null) {
                this.defaultParameters.put(param, LinearValue.ZERO);
            } else {
                String decimalFormat = config.getString("decimal-format");
                if (decimalFormat != null) {
                    this.decimalFormats.put(param, new DecimalFormat(decimalFormat));
                }
                this.defaultParameters.put(param, new LinearValue(section));
            }
        }
    }

    public SkillHandler<?> getHandler() {
        return this.handler;
    }

    public Component getName() {
        return this.name;
    }

    public List<Component> getLore() {
        return this.lore;
    }

    public ItemStack getIcon() {
        return this.icon.clone();
    }

    public boolean hasParameter(String parameter) {
        return this.defaultParameters.containsKey(parameter);
    }

    public void addParameter(String parameter, LinearValue linear) {
        this.defaultParameters.put(parameter, linear);
    }

    public double getParameter(String modifier, int level) {
        return this.defaultParameters.get(modifier).calculate(level);
    }

    public DecimalFormat getDecimalFormat(FBukkitPlugin plugin, String parameter) {
        return this.decimalFormats.getOrDefault(parameter, plugin.getConfiguration().getDecimalFormat());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof RegisteredSkill)) {
            return false;
        }
        RegisteredSkill other = (RegisteredSkill) o;
        return this.handler.equals(other.handler);
    }

    @Override
    public int hashCode() {
        return this.handler.hashCode();
    }
}