package me.kubbidev.multiversus.model.manager;

import java.util.Collection;
import java.util.Map;
import java.util.function.Function;

/**
 * A class which manages instances of a class
 *
 * @param <I> the class used to identify each object held in this manager
 * @param <C> the super class being managed
 * @param <T> the implementation class this manager is "managing"
 */
public interface Manager<I, C, T extends C> extends Function<I, T> {

    /**
     * Gets a map containing all cached instances held by this manager.
     *
     * @return all instances held in this manager
     */
    Map<I, T> getAll();

    /**
     * Gets or creates an object by id
     *
     * <p>Should only every be called by the storage implementation.</p>
     *
     * @param id The id to search by
     * @return a {@link T} object if the object is loaded or makes and returns a new object
     */
    T getOrMake(I id);

    /**
     * Get an object by id
     *
     * @param id The id to search by
     * @return a {@link T} object if the object is loaded, returns null if the object is not loaded
     */
    T getIfLoaded(I id);

    /**
     * Check to see if a object is loaded or not
     *
     * @param id The id of the object
     * @return true if the object is loaded
     */
    boolean isLoaded(I id);

    /**
     * Removes and unloads the object from the manager
     *
     * @param id The object id to unload
     */
    void unload(I id);

    /**
     * Calls {@link #unload(Object)} for all objects currently
     * loaded not in the given collection of ids.
     *
     * @param ids the ids to retain
     */
    void retainAll(Collection<I> ids);

}