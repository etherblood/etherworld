package com.etherblood.etherworld.engine.collision;

import com.etherblood.etherworld.engine.components.Speed;

public enum Axis {
    X, Y;

    public Speed set(Speed speed, int value) {
        if (this == X) {
            return new Speed(value, speed.y());
        }
        return new Speed(speed.x(), value);
    }

    public int get(Speed speed) {
        if (this == X) {
            return speed.x();
        }
        return speed.y();
    }
}
