package com.etherblood.etherworld.engine.math;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class FractionTest {

    @Test
    public void reduce() {
        // given
        Fraction fraction = new Fraction(5, 15);

        // when
        Fraction reduced = fraction.reduce();

        // then
        assertEquals(1, reduced.numerator());
        assertEquals(3, reduced.denominator());
    }

    @Test
    public void reduceZero() {
        // given
        Fraction fraction = new Fraction(0, 15);

        // when
        Fraction reduced = fraction.reduce();

        // then
        assertEquals(0, reduced.numerator());
        assertEquals(1, reduced.denominator());
    }

    @Test
    public void reduceNegative() {
        // given
        Fraction fraction = new Fraction(-15, 3);

        // when
        Fraction reduced = fraction.reduce();

        // then
        assertEquals(-5, reduced.numerator());
        assertEquals(1, reduced.denominator());
    }

    @Test
    public void multiply() {
        // given
        Fraction a = new Fraction(1, 3);
        Fraction b = new Fraction(5, 4);

        // when
        Fraction result = a.multiply(b);

        // then
        assertEquals(5, result.numerator());
        assertEquals(12, result.denominator());
    }

    @Test
    public void divide() {
        // given
        Fraction a = new Fraction(1, 3);
        Fraction b = new Fraction(5, 4);

        // when
        Fraction result = a.divide(b);

        // then
        assertEquals(4, result.numerator());
        assertEquals(15, result.denominator());
    }
}
