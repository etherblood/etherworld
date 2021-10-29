package com.etherblood.etherworld.engine.collision;

import com.etherblood.etherworld.engine.math.Fraction;

public record Collision(
        Body a,
        Body b,
        Fraction timeOfIntersection,
        AxisDirection normal) implements Comparable<Collision> {

    @Override
    public int compareTo(Collision o) {
        return timeOfIntersection.compareTo(o.timeOfIntersection());
    }
}
