package com.etherblood.etherworld.engine.sprites;

import com.etherblood.etherworld.engine.components.Hitbox;

public record SpriteFrame(
        int durationTicks,
        Hitbox[] attacks
) {
}
