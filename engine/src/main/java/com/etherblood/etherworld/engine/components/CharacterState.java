package com.etherblood.etherworld.engine.components;

import com.etherblood.etherworld.engine.EntityState;
import java.util.Objects;

public record CharacterState(
        EntityState value,
        long startTick
) {
    public CharacterState {
        Objects.requireNonNull(value);
    }
}
