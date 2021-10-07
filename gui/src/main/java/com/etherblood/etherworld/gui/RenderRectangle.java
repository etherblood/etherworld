package com.etherblood.etherworld.gui;

public record RenderRectangle(int x, int y, int width, int height) {
    public int aX() {
        return x;
    }

    public int aY() {
        return y;
    }

    public int bX() {
        return x + width;
    }

    public int bY() {
        return y + height;
    }

    public RenderRectangle translate(int x, int y) {
        return new RenderRectangle(this.x + x, this.y + y, width, height);
    }
}
