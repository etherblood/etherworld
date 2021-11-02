package com.etherblood.etherworld.engine.collision;

import com.etherblood.etherworld.engine.RectangleHitbox;
import com.etherblood.etherworld.engine.components.Position;
import com.etherblood.etherworld.engine.components.Speed;
import com.etherblood.etherworld.engine.math.Fraction;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.comparesEqualTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class BodyTest {

    @Test
    public void noIntersectionX() {
        // given
        Body a = new Body();
        a.id = 1;
        a.hitbox = new RectangleHitbox(0, 0, 100, 100);
        a.speed = new Speed(0, 0);
        a.setPosition(new Position(-100, 0));
        a.simulatedTime = Fraction.ofInt(0);

        Body b = new Body();
        b.id = 2;
        b.hitbox = new RectangleHitbox(0, 0, 100, 100);
        b.speed = new Speed(0, 10);
        b.setPosition(new Position(0, 0));
        b.simulatedTime = Fraction.ofInt(0);

        // when
        IntersectionInfo aIntersectB = a.intersect(b);
        IntersectionInfo bIntersectA = b.intersect(a);

        // then
        assertFalse(aIntersectB.isIntersecting());
        assertFalse(bIntersectA.isIntersecting());
    }

    @Test
    public void noIntersectionY() {
        // given
        Body a = new Body();
        a.id = 1;
        a.hitbox = new RectangleHitbox(0, 0, 100, 100);
        a.speed = new Speed(0, 0);
        a.setPosition(new Position(0, -100));
        a.simulatedTime = Fraction.ofInt(0);

        Body b = new Body();
        b.id = 2;
        b.hitbox = new RectangleHitbox(0, 0, 100, 100);
        b.speed = new Speed(10, 0);
        b.setPosition(new Position(0, 0));
        b.simulatedTime = Fraction.ofInt(0);

        // when
        IntersectionInfo aIntersectB = a.intersect(b);
        IntersectionInfo bIntersectA = b.intersect(a);

        // then
        assertFalse(aIntersectB.isIntersecting());
        assertFalse(bIntersectA.isIntersecting());
    }

    @Test
    public void noIntersectionCornerY() {
        // given
        Body a = new Body();
        a.id = 1;
        a.hitbox = new RectangleHitbox(0, 0, 100, 100);
        a.speed = new Speed(0, 10);
        a.setPosition(new Position(100, -100));
        a.simulatedTime = Fraction.ofInt(0);

        Body b = new Body();
        b.id = 2;
        b.hitbox = new RectangleHitbox(0, 0, 100, 100);
        b.speed = new Speed(0, 0);
        b.setPosition(new Position(0, 0));
        b.simulatedTime = Fraction.ofInt(0);

        // when
        IntersectionInfo aIntersectB = a.intersect(b);
        IntersectionInfo bIntersectA = b.intersect(a);

        // then
        assertFalse(aIntersectB.isIntersecting());
        assertFalse(bIntersectA.isIntersecting());
    }

    @Test
    public void noIntersectionCornerDiagonal() {
        // given
        Body a = new Body();
        a.id = 1;
        a.hitbox = new RectangleHitbox(0, -200, 100, 100);
        a.speed = new Speed(100, 100);
        a.setPosition(new Position(100, -100));
        a.simulatedTime = Fraction.ofInt(0);

        Body b = new Body();
        b.id = 2;
        b.hitbox = new RectangleHitbox(0, 0, 100, 100);
        b.speed = new Speed(0, 0);
        b.setPosition(new Position(0, 0));
        b.simulatedTime = Fraction.ofInt(0);

        // when
        IntersectionInfo aIntersectB = a.intersect(b);
        IntersectionInfo bIntersectA = b.intersect(a);

        // then
        assertFalse(aIntersectB.isIntersecting());
        assertFalse(bIntersectA.isIntersecting());
    }

    @Test
    public void intersection() {
        // given
        Body a = new Body();
        a.id = 1;
        a.hitbox = new RectangleHitbox(0, 0, 250, 250);
        a.speed = new Speed(100, 150);
        a.setPosition(new Position(-300, -300));
        a.simulatedTime = Fraction.ofInt(0);

        Body b = new Body();
        b.id = 2;
        b.hitbox = new RectangleHitbox(0, 0, 250, 250);
        b.speed = new Speed(0, 50);
        b.setPosition(new Position(-100, 0));
        b.simulatedTime = Fraction.ofInt(0);

        // when
        boolean boundsIntersect = a.moveBounds().intersects(b.moveBounds());
        IntersectionInfo intersect = a.intersect(b);

        // then
        assertTrue(boundsIntersect);
        assertTrue(intersect.isIntersecting());
        assertThat(intersect.time(), comparesEqualTo(new Fraction(1, 2)));
        assertEquals(1, intersect.normal().y());
    }

    @Test
    public void intersection2() {
        // given
        Body a = new Body();
        a.id = 1;
        a.hitbox = new RectangleHitbox(0, 0, 200, 200);
        a.speed = new Speed(100, 0);
        a.setPosition(new Position(-200, 0));
        a.simulatedTime = Fraction.ofInt(0);

        Body b = new Body();
        b.id = 2;
        b.hitbox = new RectangleHitbox(0, 0, 200, 200);
        b.speed = new Speed(10, 50);
        b.setPosition(new Position(0, 0));
        b.simulatedTime = Fraction.ofInt(0);

        // when
        boolean boundsIntersect = a.moveBounds().intersects(b.moveBounds());
        IntersectionInfo intersect = b.intersect(a);

        // then
        assertTrue(boundsIntersect);
        assertTrue(intersect.isIntersecting());
        assertThat(intersect.time(), comparesEqualTo(Fraction.ofInt(0)));
        assertEquals(-1, intersect.normal().x());
    }
}
