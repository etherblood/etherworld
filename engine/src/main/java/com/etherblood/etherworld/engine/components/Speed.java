package com.etherblood.etherworld.engine.components;

public record Speed(int x, int y) {

    public Speed add(Speed other) {
        return new Speed(x + other.x(), y + other.y());
    }

    public Speed subtract(Speed other) {
        return new Speed(x - other.x(), y - other.y());
    }
}
