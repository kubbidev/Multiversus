package me.kubbidev.multiversus.core.metadata.cooldown;

import java.util.OptionalLong;
import java.util.concurrent.TimeUnit;

class CooldownImpl implements Cooldown {

    // when the last test occurred.
    private long lastTested;

    // the cooldown duration in millis
    private final long timeout;

    CooldownImpl(long amount, TimeUnit unit) {
        this.timeout = unit.toMillis(amount);
        this.lastTested = 0;
    }

    @Override
    public OptionalLong getLastTested() {
        return this.lastTested == 0 ? OptionalLong.empty() : OptionalLong.of(this.lastTested);
    }

    @Override
    public void setLastTested(long time) {
        this.lastTested = Math.max(time, 0);
    }

    @Override
    public long getTimeout() {
        return this.timeout;
    }

    @Override
    public Cooldown copy() {
        return new CooldownImpl(this.timeout, TimeUnit.MILLISECONDS);
    }
}