package com.etherblood.etherworld.spriteloader.aseprite;

public record AseRectangle(
        int x,
        int y,
        int w,
        int h) {

    public AseRectangle scale(int factor) {
        return new AseRectangle(x * factor, y * factor, w * factor, h * factor);
    }
}
