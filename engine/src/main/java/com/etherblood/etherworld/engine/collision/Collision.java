package com.etherblood.etherworld.engine.collision;

import com.etherblood.etherworld.engine.math.Fraction;

public record Collision(
        Body a,
        Body b,
        Fraction timeOfIntersection,
        CollisionDirection normal) {

}
