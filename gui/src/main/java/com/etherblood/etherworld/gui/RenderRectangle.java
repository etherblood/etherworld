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

    public boolean intersects(RenderRectangle other) {
        return minX() < other.maxX() && other.minX() < maxX()
                && minY() < other.maxY() && other.minY() < maxY();
    }

    private int minX() {
        return Math.min(aX(), bX());
    }

    private int maxX() {
        return Math.max(aX(), bX());
    }

    private int minY() {
        return Math.min(aY(), bY());
    }

    private int maxY() {
        return Math.max(aY(), bY());
    }

    public RenderRectangle mirrorX(int xAxis) {
        return new RenderRectangle(2 * xAxis - x, y, -width, height);
    }
}
