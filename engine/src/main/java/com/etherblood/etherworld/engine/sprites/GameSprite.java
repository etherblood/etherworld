package com.etherblood.etherworld.engine.sprites;

import com.etherblood.etherworld.engine.RectangleHitbox;
import java.util.Map;

public record GameSprite(
        String id,
        Map<String, GameSpriteAnimation> animations,
        RectangleHitbox hitbox
) {

}
