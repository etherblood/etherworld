package com.etherblood.etherworld.engine.sprites;

import com.etherblood.etherworld.engine.RectangleHitbox;

public record GameSpriteFrame(
        int index,
        int durationTicks,
        RectangleHitbox[] attacks
) {
}
