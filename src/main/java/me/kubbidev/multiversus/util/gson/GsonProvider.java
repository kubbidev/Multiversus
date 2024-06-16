package me.kubbidev.multiversus.util.gson;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public final class GsonProvider {

    private static final Gson NORMAL = new GsonBuilder().disableHtmlEscaping().create();
    private static final Gson PRETTY_PRINTING = new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create();

    public static Gson normal() {
        return NORMAL;
    }

    public static Gson prettyPrinting() {
        return PRETTY_PRINTING;
    }

    private GsonProvider() {
        throw new AssertionError();
    }

}