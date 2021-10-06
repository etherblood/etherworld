package com.etherblood.etherworld.spriteloader.aseprite;

import java.util.List;

public record AseSprite(
        List<AseFrame> frames,
        AseMeta meta) {
}
