package com.etherblood.etherworld.spriteloader.aseprite;

public record AseFrameTag(
        String name,
        int from,
        int to,
        AseAnimationDirection direction) {
}
