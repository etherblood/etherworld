package com.etherblood.etherworld.engine.golem.components;

import com.etherblood.etherworld.engine.golem.GolemHeadState;
import java.util.Objects;

public record GolemHeadStateKey(
        GolemHeadState value,
        long startTick
) {
    public GolemHeadStateKey {
        Objects.requireNonNull(value);
    }
}
