package com.etherblood.etherworld.engine.components;

import java.util.Objects;

public record GameCharacter(
        String id
) {
    public GameCharacter {
        Objects.requireNonNull(id);
    }
}
