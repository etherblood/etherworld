package com.etherblood.etherworld.engine.components;

import com.etherblood.etherworld.engine.RectangleHitbox;
import java.util.Objects;

public record Movebox(
        RectangleHitbox hitbox
) {
    public Movebox {
        Objects.requireNonNull(hitbox);
    }
}
