package com.etherblood.etherworld.engine.characters.components;

import com.etherblood.etherworld.engine.characters.CharacterState;
import java.util.Objects;

public record CharacterStateKey(
        CharacterState value,
        long startTick
) {
    public CharacterStateKey {
        Objects.requireNonNull(value);
    }
}
