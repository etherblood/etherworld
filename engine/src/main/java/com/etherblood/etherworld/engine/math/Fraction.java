package com.etherblood.etherworld.engine.math;

public record Fraction(long numerator, long denominator) implements Comparable<Fraction> {

    public Fraction(long numerator, long denominator) {
        if (denominator < 0) {
            this.numerator = -numerator;
            this.denominator = -denominator;
        } else {
            this.numerator = numerator;
            this.denominator = denominator;
        }
    }

    public static Fraction negativeInfinity() {
        return new Fraction(-1, 0);
    }

    public static Fraction positiveInfinity() {
        return new Fraction(1, 0);
    }

    public static Fraction nan() {
        return new Fraction(0, 0);
    }

    @Override
    public int compareTo(Fraction other) {
        if (denominator == other.denominator()) {
            // this also handles the case of comparing fractions with denominator 0
            return Long.compare(numerator, other.numerator());
        }
        return Long.compare(numerator * other.denominator(), other.numerator() * denominator);
    }

    public Fraction add(Fraction other) {
        return new Fraction(numerator * other.denominator() + other.numerator() * denominator, denominator * other.denominator());
    }

    public Fraction subtract(Fraction other) {
        return new Fraction(numerator * other.denominator() - other.numerator() * denominator, denominator * other.denominator());
    }

    public Fraction multiply(Fraction other) {
        return new Fraction(numerator * other.numerator(), denominator * other.denominator());
    }

    public Fraction divide(Fraction other) {
        return new Fraction(numerator * other.denominator(), denominator * other.numerator());
    }

    public Fraction reduce() {
        long gcd = MathUtil.gcd(numerator, denominator);
        return new Fraction(numerator / gcd, denominator / gcd);
    }

    public static Fraction ofInt(int numerator) {
        return new Fraction(numerator, 1);
    }

    public double toDouble() {
        return (double) numerator / denominator;
    }

    public int downToInt() {
        return (int) (numerator / denominator);
    }

    public int toIntExact() {
        if (numerator % denominator != 0) {
            throw new ArithmeticException("Fraction can not be converted to int.");
        }
        return (int) (numerator / denominator);
    }

    public int floorToInt() {
        return (int) (Math.floorDiv(numerator, denominator));
    }

    public int ceilToInt() {
        return (int) (MathUtil.ceilDiv(numerator, denominator));
    }

    public int upToInt() {
        return numerator >= 0 ? ceilToInt() : floorToInt();
    }
}

