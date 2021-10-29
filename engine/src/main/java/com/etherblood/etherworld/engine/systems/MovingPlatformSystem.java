package com.etherblood.etherworld.engine.systems;

import com.etherblood.etherworld.data.EntityData;
import com.etherblood.etherworld.engine.Etherworld;
import com.etherblood.etherworld.engine.PlayerAction;
import com.etherblood.etherworld.engine.RectangleHitbox;
import com.etherblood.etherworld.engine.components.MovingPlatform;
import com.etherblood.etherworld.engine.components.Position;
import com.etherblood.etherworld.engine.components.Speed;
import java.util.Map;
import java.util.Set;

public class MovingPlatformSystem implements GameSystem {

    @Override
    public void tick(Etherworld world, Map<Integer, Set<PlayerAction>> playerActions) {
        EntityData data = world.getData();

        for (int platform : data.list(MovingPlatform.class)) {
            Position position = data.get(platform, Position.class);
            MovingPlatform value = data.get(platform, MovingPlatform.class);

            Position nextPos = calcPosition(value.path(), value.speed(), world.getTick());
            int vx = nextPos.x() - position.x();
            int vy = nextPos.y() - position.y();

            data.set(platform, new Speed(vx, vy));
        }
    }

    private static Position calcPosition(RectangleHitbox path, int speed, long elapsedTicks) {
        int x = 0;
        int y = 0;
        int totalDistance = Math.floorMod(speed * elapsedTicks, 2 * path.width() + 2 * path.height());
        if (totalDistance >= path.width()) {
            x += path.width();
            totalDistance -= path.width();
            if (totalDistance >= path.height()) {
                y += path.height();
                totalDistance -= path.height();
                if (totalDistance >= path.width()) {
                    x -= path.width();
                    totalDistance -= path.width();
                    y -= totalDistance;
                } else {
                    x -= totalDistance;
                }
            } else {
                y += totalDistance;
            }
        } else {
            x += totalDistance;
        }
        return new Position(path.x() + x, path.y() + y);
    }
}
