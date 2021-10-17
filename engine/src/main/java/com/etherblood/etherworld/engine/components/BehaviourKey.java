package com.etherblood.etherworld.engine.components;

import java.util.Objects;

public record BehaviourKey(
        String value,
        long startTick
) {
    public BehaviourKey {
        Objects.requireNonNull(value);
    }
}
