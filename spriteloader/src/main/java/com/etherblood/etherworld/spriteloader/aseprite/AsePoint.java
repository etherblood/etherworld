package com.etherblood.etherworld.spriteloader.aseprite;

public record AsePoint(
        int x,
        int y) {

    public AsePoint scale(int factor) {
        return new AsePoint(x * factor, y * factor);
    }
}
