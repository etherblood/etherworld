package com.etherblood.etherworld.engine.characters;

import com.etherblood.etherworld.engine.RectangleHitbox;

public record AttackParams(
        RectangleHitbox damageBox,
        int damageFrom,
        int damageTo,
        int damage,
        int attackTicks
) {
}
