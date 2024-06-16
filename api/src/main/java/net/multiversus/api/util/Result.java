package net.multiversus.api.util;

/**
 * Represents a generic result, which can either be successful or fail.
 */
@FunctionalInterface
public interface Result {

    /**
     * Instance of {@link Result} which always reports success.
     */
    Result GENERIC_SUCCESS = () -> true;

    /**
     * Instance of {@link Result} which always reports failure.
     */
    Result GENERIC_FAILURE = () -> false;

    /**
     * Gets if the operation which produced this result completed successfully.
     *
     * @return if the result indicates a success
     */
    boolean wasSuccessful();

}