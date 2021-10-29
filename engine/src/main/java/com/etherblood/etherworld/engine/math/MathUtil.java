package com.etherblood.etherworld.engine.math;

public class MathUtil {

    public static long ceilDiv(long x, long y) {
        return -Math.floorDiv(-x, y);
    }

    public static int ceilDiv(int x, int y) {
        return -Math.floorDiv(-x, y);
    }

    public static <T extends Comparable<T>> T max(T a, T b) {
        if (a.compareTo(b) < 0) {
            return b;
        }
        return a;
    }

    public static <T extends Comparable<T>> T min(T a, T b) {
        if (a.compareTo(b) >= 0) {
            return b;
        }
        return a;
    }
    
    // https://en.wikipedia.org/wiki/Euclidean_algorithm
    public static long gcd(long a, long b) {
        while (b != 0) {
            long t = b;
            b = a % b;
            a = t;
        }
        return Math.max(a, -a);
    }
}
