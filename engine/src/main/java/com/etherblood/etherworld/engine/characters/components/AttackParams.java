package com.etherblood.etherworld.engine.characters.components;

import com.etherblood.etherworld.engine.RectangleHitbox;

public record AttackParams(
        RectangleHitbox damageBox,
        int damageStart,
        int damageEnd,
        int damage,
        int attackTicks
) {
}
