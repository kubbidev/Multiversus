package me.kubbidev.multiversus.core.util;

import com.google.common.reflect.TypeToken;

/**
 * Represents an object that knows it's own type parameter.
 *
 * @param <T> the type
 */
public interface TypeAware<T> {

    TypeToken<T> getType();
}