package com.etherblood.etherworld.engine.components;

import java.util.Objects;

public record StateKey(
        String value,
        long startTick
) {
    public StateKey {
        Objects.requireNonNull(value);
    }
}
