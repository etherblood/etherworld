package com.etherblood.etherworld.engine;

public class MathUtil {

    public static int ceilDiv(int x, int y) {
        return -Math.floorDiv(-x, y);
    }
}
