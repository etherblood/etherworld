package com.etherblood.etherworld.engine.golem.components;

import com.etherblood.etherworld.engine.golem.GolemHandState;
import java.util.Objects;

public record GolemHandStateKey(
        GolemHandState value,
        long startTick
) {
    public GolemHandStateKey {
        Objects.requireNonNull(value);
    }
}
