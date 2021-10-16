package com.etherblood.etherworld.engine.components;

import java.util.Objects;

public record Respawn(Position position) {
    public Respawn {
        Objects.requireNonNull(position);
    }
}
