package com.etherblood.etherworld.engine.collision;

public enum AxisDirection {
    X_POSITIVE,
    X_NEGATIVE,
    Y_POSITIVE,
    Y_NEGATIVE;

    public Axis toAxis() {
        switch (this) {
            case X_POSITIVE:
            case X_NEGATIVE:
                return Axis.X;
            case Y_POSITIVE:
            case Y_NEGATIVE:
                return Axis.Y;
            default:
                throw new AssertionError(this);
        }
    }
}
