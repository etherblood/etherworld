package com.etherblood.etherworld.engine.characters.components;

import com.etherblood.etherworld.engine.RectangleHitbox;

public record PhysicParams(
        RectangleHitbox movebox,
        int runSpeed,
        int gravityPerTick,
        int jumpStrength,
        int hoverStrength) {
}
