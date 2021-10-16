package com.etherblood.etherworld.engine.components;

import com.etherblood.etherworld.engine.RectangleHitbox;
import java.util.Objects;

public record Obstaclebox(
        RectangleHitbox hitbox
) {
    public Obstaclebox {
        Objects.requireNonNull(hitbox);
    }
}
