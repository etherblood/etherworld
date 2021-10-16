package com.etherblood.etherworld.engine.components;

import com.etherblood.etherworld.engine.RectangleHitbox;
import java.util.Objects;

public record Hurtbox(
        RectangleHitbox hitbox
) {
    public Hurtbox {
        Objects.requireNonNull(hitbox);
    }
}
