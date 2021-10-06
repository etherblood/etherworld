package com.etherblood.etherworld.engine.sprites;

public record GameSpriteHitbox(int x, int y, int width, int height) {
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

    public GameSpriteHitbox translate(int x, int y) {
        return new GameSpriteHitbox(this.x + x, this.y + y, width, height);
    }

    public boolean intersects(GameSpriteHitbox other) {
        return minX() < other.maxX() && other.minX() < maxX()
                && minY() < other.maxY() && other.minY() < maxY();
    }
}
