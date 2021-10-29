package com.etherblood.etherworld.engine.collision;

import com.etherblood.etherworld.engine.RectangleHitbox;
import com.etherblood.etherworld.engine.components.Position;
import com.etherblood.etherworld.engine.components.Speed;
import com.etherblood.etherworld.engine.math.Fraction;
import com.etherblood.etherworld.engine.math.Interval;

public class Body {

    public Integer id;
    public Fraction simulatedTime;
    public Fraction simulatedPositionX;
    public Fraction simulatedPositionY;
    public Speed speed;
    public RectangleHitbox hitbox;

    public void setPosition(Position position) {
        simulatedPositionX = Fraction.ofInt(position.x());
        simulatedPositionY = Fraction.ofInt(position.y());
    }

    public RectangleHitbox moveBounds() {
        Fraction remainingTime = Fraction.ofInt(1).subtract(simulatedTime);
        int remainingOffsetX = remainingTime.multiply(Fraction.ofInt(speed.x())).upToInt();
        int x, width;
        if (remainingOffsetX > 0) {
            x = simulatedPositionX.floorToInt() + hitbox.x();
            width = hitbox.width() + remainingOffsetX;
        } else {
            x = simulatedPositionX.floorToInt() + hitbox.x() + remainingOffsetX;
            width = hitbox.width() - remainingOffsetX;
        }

        int remainingOffsetY = remainingTime.multiply(Fraction.ofInt(speed.y())).upToInt();
        int y, height;
        if (remainingOffsetY > 0) {
            y = simulatedPositionY.floorToInt() + hitbox.y();
            height = hitbox.height() + remainingOffsetY;
        } else {
            y = simulatedPositionY.floorToInt() + hitbox.y() + remainingOffsetY;
            height = hitbox.height() - remainingOffsetY;
        }
        return new RectangleHitbox(x, y, width, height);
    }

    public IntersectionInfo intersect(Body other) {
        Fraction negativeSimulatedOffsetX = simulatedTime.multiply(Fraction.ofInt(-speed.x()));
        Fraction negativeSimulatedOffsetY = simulatedTime.multiply(Fraction.ofInt(-speed.y()));
        FractionRectangle a = FractionRectangle.ofHitbox(hitbox)
                .translate(
                        simulatedPositionX.add(negativeSimulatedOffsetX),
                        simulatedPositionY.add(negativeSimulatedOffsetY));

        Fraction otherNegativeSimulatedOffsetX = other.simulatedTime.multiply(Fraction.ofInt(-other.speed.x()));
        Fraction otherNegativeSimulatedOffsetY = other.simulatedTime.multiply(Fraction.ofInt(-other.speed.y()));
        FractionRectangle b = FractionRectangle.ofHitbox(other.hitbox)
                .translate(
                        other.simulatedPositionX.add(otherNegativeSimulatedOffsetX),
                        other.simulatedPositionY.add(otherNegativeSimulatedOffsetY));
        return intersect(a, speed, b, other.speed);
    }

    private static IntersectionInfo intersect(FractionRectangle a, Speed va, FractionRectangle b, Speed vb) {
        Speed v = va.subtract(vb);
        Fraction vx = Fraction.ofInt(v.x());
        Fraction vy = Fraction.ofInt(v.y());

        Interval<Fraction> intersectX;
        if (v.x() != 0) {
            Fraction toiEnterX = b.minX().subtract(a.maxX()).divide(vx);
            Fraction toiLeaveX = b.maxX().subtract(a.minX()).divide(vx);
            intersectX = Interval.ofUnorderedBounds(toiEnterX, toiLeaveX);
        } else {
            if (a.maxX().compareTo(b.minX()) <= 0
                    || b.maxX().compareTo(a.minX()) <= 0) {
                return IntersectionInfo.noIntersection();
            }
            intersectX = new Interval(Fraction.negativeInfinity(), Fraction.positiveInfinity());
        }

        Interval<Fraction> intersectY;
        if (v.y() != 0) {
            Fraction toiEnterY = b.minY().subtract(a.maxY()).divide(vy);
            Fraction toiLeaveY = b.maxY().subtract(a.minY()).divide(vy);
            intersectY = Interval.ofUnorderedBounds(toiEnterY, toiLeaveY);
        } else {
            if (a.maxY().compareTo(b.minY()) <= 0
                    || b.maxY().compareTo(a.minY()) <= 0) {
                return IntersectionInfo.noIntersection();
            }
            intersectY = new Interval(Fraction.negativeInfinity(), Fraction.positiveInfinity());
        }

        Interval<Fraction> intersection = Interval.intersect(intersectX, intersectY);
        if (intersection == null) {
            return IntersectionInfo.noIntersection();
        }
        AxisDirection direction;
        Fraction intersectStart = intersection.start();
        Fraction intersectYStart = intersectY.start();
        if (intersectStart.compareTo(intersectYStart) == 0
                && v.y() != 0) {
            if (v.y() > 0) {
                direction = AxisDirection.Y_POSITIVE;
            } else {
                direction = AxisDirection.Y_NEGATIVE;
            }
        } else if (v.x() > 0) {
            direction = AxisDirection.X_POSITIVE;
        } else if (v.x() < 0) {
            direction = AxisDirection.X_NEGATIVE;
        } else {
            return IntersectionInfo.noIntersection();
        }
        return new IntersectionInfo(intersectStart, direction);
    }

    @Override
    public String toString() {
        return "Body{" +
                "id=" + id +
                '}';
    }
}
