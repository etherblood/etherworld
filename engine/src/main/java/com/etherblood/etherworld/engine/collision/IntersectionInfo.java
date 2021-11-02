package com.etherblood.etherworld.engine.collision;

import com.etherblood.etherworld.engine.math.Fraction;

public record IntersectionInfo(Fraction time, CollisionDirection normal) {

    public static IntersectionInfo noIntersection() {
        return new IntersectionInfo(null, null);
    }

    public boolean isIntersecting() {
        return time != null
                && time.compareTo(Fraction.ofInt(0)) >= 0
                && time.compareTo(Fraction.ofInt(1)) < 0;
    }
}
