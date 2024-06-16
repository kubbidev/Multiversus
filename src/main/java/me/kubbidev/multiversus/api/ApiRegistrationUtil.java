package me.kubbidev.multiversus.api;

import net.multiversus.api.Multiversus;
import net.multiversus.api.MultiversusProvider;

import java.lang.reflect.Method;

public class ApiRegistrationUtil {
    private static final Method REGISTER;
    private static final Method UNREGISTER;
    static {
        try {
            REGISTER = MultiversusProvider.class.getDeclaredMethod("register", Multiversus.class);
            REGISTER.setAccessible(true);

            UNREGISTER = MultiversusProvider.class.getDeclaredMethod("unregister");
            UNREGISTER.setAccessible(true);
        } catch (NoSuchMethodException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    public static void registerProvider(Multiversus multiversusApi) {
        try {
            REGISTER.invoke(null, multiversusApi);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void unregisterProvider() {
        try {
            UNREGISTER.invoke(null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}