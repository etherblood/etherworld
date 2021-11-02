package com.etherblood.etherworld.engine.collision;

import com.etherblood.etherworld.engine.math.Fraction;

public record Collision(
        Body a,
        Body b,
        Fraction timeOfIntersection,
        CollisionDirection normal) implements Comparable<Collision> {

    @Override
    public int compareTo(Collision o) {
        int compare = timeOfIntersection.compareTo(o.timeOfIntersection());
        if (compare == 0) {
            compare = Integer.compare(normal().priority(), o.normal.priority());
        }
        return compare;
    }
}
