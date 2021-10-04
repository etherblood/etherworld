package com.etherblood.etherworld.engine.sprites;

import java.util.Arrays;

public record SpriteAnimation(
        SpriteFrame[] frames
) {
    public int totalTicks() {
        return Arrays.stream(frames).mapToInt(SpriteFrame::durationTicks).sum();
    }
}
