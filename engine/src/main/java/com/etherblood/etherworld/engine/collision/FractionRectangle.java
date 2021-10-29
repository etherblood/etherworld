package com.etherblood.etherworld.engine.collision;

import com.etherblood.etherworld.engine.RectangleHitbox;
import com.etherblood.etherworld.engine.math.Fraction;

public record FractionRectangle(
        Fraction x, Fraction y,
        Fraction width, Fraction height
) {

    public static FractionRectangle ofHitbox(RectangleHitbox hitbox) {
        return new FractionRectangle(
                Fraction.ofInt(hitbox.x()),
                Fraction.ofInt(hitbox.y()),
                Fraction.ofInt(hitbox.width()),
                Fraction.ofInt(hitbox.height())
        );
    }

    public Fraction minX() {
        return x;
    }

    public Fraction minY() {
        return y;
    }

    public Fraction maxX() {
        return x.add(width);
    }

    public Fraction maxY() {
        return y.add(height);
    }

    public FractionRectangle translate(Fraction x, Fraction y) {
        return new FractionRectangle(this.x.add(x), this.y.add(y), width, height);
    }
}
