package com.etherblood.etherworld.engine.characters;

public record PhysicParams(
        int runSpeed,
        int gravityPerTick,
        int jumpStrength,
        int hoverStrength) {
}
