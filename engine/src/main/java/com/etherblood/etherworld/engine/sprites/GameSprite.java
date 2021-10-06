package com.etherblood.etherworld.engine.sprites;

import java.util.Map;

public record GameSprite(
        String id,
        Map<String, GameSpriteAnimation> animations,
        GameSpriteHitbox hitbox
) {

}
