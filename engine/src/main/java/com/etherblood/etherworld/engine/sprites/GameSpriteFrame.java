package com.etherblood.etherworld.engine.sprites;

public record GameSpriteFrame(
        int index,
        int durationTicks,
        GameSpriteHitbox[] attacks
) {
}
