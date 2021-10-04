package com.etherblood.etherworld.engine.components;

public record Hitbox(int x, int y, int width, int height) {
    public int minX() {
        return x;
    }

    public int minY() {
        return y;
    }

    public int maxX() {
        return x + width;
    }

    public int maxY() {
        return y + height;
    }

    public Hitbox translate(int x, int y) {
        return new Hitbox(this.x + x, this.y + y, width, height);
    }

    public boolean intersects(Hitbox other) {
        return minX() < other.maxX() && other.minX() < maxX()
                && minY() < other.maxY() && other.minY() < maxY();
    }
}
