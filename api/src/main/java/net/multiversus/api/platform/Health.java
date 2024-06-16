package net.multiversus.api.platform;

import java.util.Map;

/**
 * Represents the "health" status (healthcheck) of a Multiversus implementation.
 */
public interface Health {

    /**
     * Gets if Multiversus is healthy.
     *
     * @return if Multiversus is healthy
     */
    boolean isHealthy();

    /**
     * Gets extra metadata/details about the healthcheck result.
     *
     * @return details about the healthcheck status
     */
    Map<String, Object> getDetails();

}