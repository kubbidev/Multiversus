package me.kubbidev.multiversus.command.spec;

import me.kubbidev.multiversus.locale.Message;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TranslatableComponent;

public class Argument {
    private final String name;
    private final boolean required;
    private final TranslatableComponent description;

    Argument(String name, boolean required, TranslatableComponent description) {
        this.name = name;
        this.required = required;
        this.description = description;
    }

    public String getName() {
        return this.name;
    }

    public boolean isRequired() {
        return this.required;
    }

    public TranslatableComponent getDescription() {
        return this.description;
    }

    public Component asPrettyString() {
        return (this.required ? Message.REQUIRED_ARGUMENT : Message.OPTIONAL_ARGUMENT).build(this.name);
    }
}