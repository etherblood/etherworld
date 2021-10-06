package com.etherblood.etherworld.engine.sprites;

import java.util.Arrays;

public record GameSpriteAnimation(
        GameSpriteFrame[] frames
) {
    public int totalTicks() {
        return Arrays.stream(frames).mapToInt(GameSpriteFrame::durationTicks).sum();
    }
}
