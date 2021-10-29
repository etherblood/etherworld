package com.etherblood.etherworld.engine.math;

public record Interval<T extends Comparable<T>>(T start, T end) {
    public Interval {
        if (start.compareTo(end) >= 0) {
            throw new AssertionError("Start (" + start + ") must be before end (" + end + ").");
        }
    }

    public static <T extends Comparable<T>> Interval<T> ofUnorderedBounds(T a, T b) {
        int compare = a.compareTo(b);
        if (compare > 0) {
            return new Interval<>(b, a);
        }
        if (compare < 0) {
            return new Interval<>(a, b);
        }
        return null;
    }

    public static <T extends Comparable<T>> Interval<T> ofBounds(T a, T b) {
        int compare = a.compareTo(b);
        if (compare < 0) {
            return new Interval<>(a, b);
        }
        return null;
    }

    public static <T extends Comparable<T>> Interval<T> intersect(Interval<T> a, Interval<T> b) {
        if (a == null || b == null) {
            return null;
        }
        T start = MathUtil.max(a.start(), b.start());
        T end = MathUtil.min(a.end(), b.end());
        if (start.compareTo(end) >= 0) {
            return null;
        }
        return new Interval<>(start, end);
    }
}
