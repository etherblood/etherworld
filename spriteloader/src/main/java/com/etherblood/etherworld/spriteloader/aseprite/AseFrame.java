package com.etherblood.etherworld.spriteloader.aseprite;

public record AseFrame(
        String filename,
        AseRectangle frame,
        boolean rotated,
        boolean trimmed,
        AseRectangle spriteSourceSize,
        AseSize sourceSize,
        int duration) {
}
