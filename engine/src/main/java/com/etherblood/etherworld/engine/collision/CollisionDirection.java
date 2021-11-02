package com.etherblood.etherworld.engine.collision;

public record CollisionDirection(int x, int y) {
    public CollisionDirection(int x, int y) {
        this.x = Integer.signum(x);
        this.y = Integer.signum(y);
        if (x == 0 && y == 0) {
            throw new ArithmeticException("Direction cant have length 0.");
        }
    }

    public int priority() {
        if (x != 0 && y != 0) {
            return 2;
        }
        if (y != 0) {
            return 1;
        }
        return 0;
    }

    public Axis toAxis() {
        if (x != 0) {
            return Axis.X;
        }
        return Axis.Y;
    }
}
