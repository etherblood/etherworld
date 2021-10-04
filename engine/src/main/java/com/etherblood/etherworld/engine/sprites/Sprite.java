package com.etherblood.etherworld.engine.sprites;

import java.util.Map;

public record Sprite(
        String id,
        Map<String, SpriteAnimation> animations,
        SpriteHitbox hitbox
) {

}
