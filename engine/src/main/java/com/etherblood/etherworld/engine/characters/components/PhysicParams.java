package com.etherblood.etherworld.engine.characters.components;

public record PhysicParams(
        int runSpeed,
        int gravityPerTick,
        int jumpStrength,
        int hoverStrength) {
}
