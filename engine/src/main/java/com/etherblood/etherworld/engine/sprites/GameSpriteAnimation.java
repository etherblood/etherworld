package com.etherblood.etherworld.engine.sprites;

import java.util.Arrays;

public record GameSpriteAnimation(
        GameSpriteFrame[] frames
) {
    public int totalTicks() {
        return Arrays.stream(frames).mapToInt(GameSpriteFrame::durationTicks).sum();
    }

    public GameSpriteFrame frameByTick(int tick) {
        tick = Math.floorMod(tick, totalTicks());
        int i = -1;
        do {
            i++;
            tick -= frames[i].durationTicks();
        } while (tick >= 0);
        return frames[i];
    }
}
