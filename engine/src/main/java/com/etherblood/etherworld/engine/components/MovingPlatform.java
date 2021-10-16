package com.etherblood.etherworld.engine.components;

import com.etherblood.etherworld.engine.RectangleHitbox;
import java.util.Objects;

public record MovingPlatform(
        String id,
        RectangleHitbox path,
        int speed
) {
    public MovingPlatform {
        Objects.requireNonNull(id);
        Objects.requireNonNull(path);
        if (path.width() % Math.abs(speed) != 0) {
            throw new IllegalArgumentException("path.width must be a multiple of abs(speed).");
        }
        if (path.height() % Math.abs(speed) != 0) {
            throw new IllegalArgumentException("path.height must be a multiple of abs(speed).");
        }
    }
}
