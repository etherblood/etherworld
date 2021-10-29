package com.etherblood.etherworld.engine;

import com.etherblood.etherworld.engine.components.Position;

public record RectangleHitbox(int x, int y, int width, int height) {
    public RectangleHitbox {
        if (width <= 0) {
            throw new IllegalArgumentException("width " + width + " is not positive.");
        }
        if (height <= 0) {
            throw new IllegalArgumentException("height " + height + " is not positive.");
        }
    }

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

    public RectangleHitbox translate(Position position) {
        return translate(position.x(), position.y());
    }

    public RectangleHitbox translate(int x, int y) {
        return new RectangleHitbox(this.x + x, this.y + y, width, height);
    }

    public boolean intersects(RectangleHitbox other) {
        return minX() < other.maxX() && other.minX() < maxX()
                && minY() < other.maxY() && other.minY() < maxY();
    }

    public RectangleHitbox mirrorX(int xAxis) {
        return new RectangleHitbox(2 * xAxis - x - width, y, width, height);
    }

    public boolean contains(RectangleHitbox other) {
        return minX() <= other.minX() && other.maxX() <= maxX()
                && minY() <= other.minY() && other.maxY() <= maxY();
    }
}
