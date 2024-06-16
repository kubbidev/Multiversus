package me.kubbidev.multiversus.plugin.util;

import com.google.gson.Gson;
import net.multiversus.api.platform.Health;

import java.util.Map;

public class HealthCheckResult implements Health {
    private static final Gson GSON = new Gson();

    public static HealthCheckResult healthy(Map<String, Object> details) {
        return new HealthCheckResult(true, details);
    }

    public static HealthCheckResult unhealthy(Map<String, Object> details) {
        return new HealthCheckResult(false, details);
    }

    private final boolean healthy;
    private final Map<String, Object> details;

    HealthCheckResult(boolean healthy, Map<String, Object> details) {
        this.healthy = healthy;
        this.details = details;
    }

    @Override
    public boolean isHealthy() {
        return this.healthy;
    }

    @Override
    public Map<String, Object> getDetails() {
        return this.details;
    }

    @Override
    public String toString() {
        return GSON.toJson(this);
    }

}