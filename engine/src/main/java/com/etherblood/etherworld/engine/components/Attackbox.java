package com.etherblood.etherworld.engine.components;

import com.etherblood.etherworld.engine.RectangleHitbox;
import java.util.Objects;

public record Attackbox(
        RectangleHitbox hitbox,
        int damage
) {
    public Attackbox {
        Objects.requireNonNull(hitbox);
    }
}
