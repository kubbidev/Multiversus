package me.kubbidev.multiversus.core.manager;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import me.kubbidev.multiversus.FBukkitPlugin;
import me.kubbidev.multiversus.core.UtilityMethod;
import me.kubbidev.multiversus.core.skill.RegisteredSkill;
import me.kubbidev.multiversus.core.skill.handler.SkillHandler;
import me.kubbidev.multiversus.util.MoreFiles;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.*;

public final class SkillManager {
    private final FBukkitPlugin plugin;

    /**
     * All registered skill handlers accessible by any external plugins.
     */
    private final Map<String, SkillHandler<?>> handlers = new HashMap<>();

    /**
     * All registered skills with their default parameters value set.
     */
    private final Map<String, RegisteredSkill> skills = new LinkedHashMap<>();

    private boolean registration = true;

    public SkillManager(FBukkitPlugin plugin) {
        this.plugin = plugin;
    }

    public void registerSkillHandler(SkillHandler<?> handler) {
        Preconditions.checkArgument(this.handlers.putIfAbsent(handler.getId(), handler) == null,
                "A skill handler with the same name already exists");

        if (!this.registration && handler instanceof Listener)
            Bukkit.getPluginManager().registerEvents((Listener) handler, this.plugin.getLoader());
    }

    public SkillHandler<?> getHandlerOrThrow(String id) {
        return Objects.requireNonNull(this.handlers.get(id), "Could not find handler with ID '" + id + "'");
    }

    public @Nullable SkillHandler<?> getHandler(String handlerId) {
        return this.handlers.get(handlerId);
    }

    /**
     * @return Currently registered skill handlers.
     */
    public Collection<SkillHandler<?>> getHandlers() {
        return this.handlers.values();
    }

    public void registerSkill(RegisteredSkill skill) {
        Preconditions.checkArgument(this.skills.putIfAbsent(skill.getHandler().getId(), skill) == null,
                "A skill with the same name already exists");
    }

    public RegisteredSkill getSkillOrThrow(String id) {
        return Objects.requireNonNull(this.skills.get(id), "Could not find skill with ID '" + id + "'");
    }

    public @Nullable RegisteredSkill getSkill(String skillId) {
        return this.skills.get(skillId);
    }

    /**
     * @return Currently registered skills.
     */
    public Collection<RegisteredSkill> getSkills() {
        return this.skills.values();
    }

    public void load(boolean clearBefore) {
        Path skillsPath = this.plugin.getBootstrap().getConfigDirectory().resolve("skill");
        if (clearBefore) {
            this.handlers.values().stream().filter(handler -> handler instanceof Listener).map(handler -> (Listener) handler)
                    .forEach(HandlerList::unregisterAll);

            this.handlers.clear();
        } else {
            this.registration = false;
            try {
                MoreFiles.createDirectoriesIfNotExists(skillsPath);
            } catch (IOException e) {
                // ignore
            }
        }

        for (SkillHandler<?> handler : getHandlers()) {
            File handlerFile = skillsPath.resolve(handler.getId() + ".yml").toFile();
            FileConfiguration config = YamlConfiguration.loadConfiguration(handlerFile);

            // if the skill configuration don't already exists (empty) fill it with default value
            if (!handlerFile.exists()) {
                config.set("name", UtilityMethod.caseOnWords(handler.getId().toLowerCase(Locale.ROOT)
                        .replace("_", " ")
                        .replace("-", " ")));

                config.set("lore", ImmutableList.<String>builder()
                                .add("This is the default skill description")
                                .add("The description support MiniMessage!")
                                .add("")
                                .add("<cooldown>s cooldown")
                        .build());
                config.set("icon", "BOOK");
                try {
                    for (String parameter : handler.getParameters()) {
                        config.set(parameter + ".base", 0);
                        config.set(parameter + ".per-level", 0);
                        config.set(parameter + ".min", 0);
                        config.set(parameter + ".max", 0);
                    }
                    config.save(handlerFile);
                } catch (IOException e) {
                    this.plugin.getLogger().severe("Could not save " + handler.getId() + ".yml: " + e.getMessage());
                }
            }
            try {
                RegisteredSkill skill = new RegisteredSkill(handler, config);
                registerSkill(skill);
            } catch (RuntimeException e) {
                this.plugin.getLogger().warn("Could not load skill '" + handler.getId() + "': " + e.getMessage());
            }
        }
    }
}