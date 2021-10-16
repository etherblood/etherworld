package com.etherblood.etherworld.engine.components;

public record Health(
        int value,
        int max) {
    
    public Health {
        if (value < 0) {
            throw new AssertionError("Health must be not negative.");
        }
        if (max < value) {
            throw new AssertionError("Health must not be greater than max.");
        }
    }

    public Health damage(int damage) {
        return new Health(Math.max(0, value - damage), max);
    }
}
