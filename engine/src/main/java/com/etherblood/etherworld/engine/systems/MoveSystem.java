package com.etherblood.etherworld.engine.systems;

import com.etherblood.etherworld.data.EntityData;
import com.etherblood.etherworld.engine.Etherworld;
import com.etherblood.etherworld.engine.PlayerAction;
import com.etherblood.etherworld.engine.RectangleHitbox;
import com.etherblood.etherworld.engine.chunks.Chunk;
import com.etherblood.etherworld.engine.chunks.ChunkManager;
import com.etherblood.etherworld.engine.chunks.ChunkPosition;
import com.etherblood.etherworld.engine.chunks.LocalTilePosition;
import com.etherblood.etherworld.engine.chunks.TilePosition;
import com.etherblood.etherworld.engine.components.Movebox;
import com.etherblood.etherworld.engine.components.MovingPlatform;
import com.etherblood.etherworld.engine.components.Obstaclebox;
import com.etherblood.etherworld.engine.components.OnGround;
import com.etherblood.etherworld.engine.components.OnPlatform;
import com.etherblood.etherworld.engine.components.Position;
import com.etherblood.etherworld.engine.components.Speed;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class MoveSystem implements GameSystem {

    @Override
    public void tick(Etherworld world, Map<Integer, Set<PlayerAction>> playerActions) {
        EntityData data = world.getData();
        ChunkManager chunks = world.getChunks();

        for (int platform : data.list(MovingPlatform.class)) {
            Position position = data.get(platform, Position.class);
            MovingPlatform value = data.get(platform, MovingPlatform.class);

            Position nextPos = calcPosition(value.path(), value.speed(), world.getTick());
            int vx = nextPos.x() - position.x();
            int vy = nextPos.y() - position.y();

            for (int other : data.findByValue(new OnPlatform(platform))) {
                data.set(other, data.get(other, Position.class).translate(vx, vy));
                data.remove(other, OnPlatform.class);
            }
            data.set(platform, position.translate(vx, vy));
        }

        for (int entity : data.list(Speed.class)) {
            Speed speed = data.get(entity, Speed.class);
            Position position = data.get(entity, Position.class);
            Movebox movebox = data.get(entity, Movebox.class);
            if (movebox != null) {
                RectangleHitbox hitbox = movebox.hitbox();
                int vx = speed.x();
                int vy = speed.y();

                boolean grounded = false;


                boolean collideX = false;
                boolean collideY = false;

                int roiMinX = Math.min(vx, 0) + position.x() + hitbox.minX();
                int roiMaxX = Math.max(vx, 0) + position.x() + hitbox.maxX();
                int roiMinY = Math.min(vy, 0) + position.y() + hitbox.minY();
                int roiMaxY = Math.max(vy, 0) + position.y() + hitbox.maxY();

                TilePosition minTile = chunks.toFloorTilePosition(new Position(roiMinX, roiMinY));
                TilePosition maxTile = chunks.toCeilTilePosition(new Position(roiMaxX, roiMaxY));

                List<RectangleHitbox> obstacles = new ArrayList<>();
                for (int y = minTile.y(); y < maxTile.y(); y++) {
                    for (int x = minTile.x(); x < maxTile.x(); x++) {
                        TilePosition tilePosition = new TilePosition(x, y);
                        ChunkPosition chunkPosition = chunks.toChunkPosition(tilePosition);
                        Chunk chunk = chunks.get(chunkPosition);
                        if (chunk != null) {
                            LocalTilePosition tile = chunks.toLocalChunkPosition(tilePosition);
                            if (chunk.isObstacle(tile)) {
                                int scale = chunks.getTileSize();
                                obstacles.add(new RectangleHitbox(
                                        x * scale,
                                        y * scale,
                                        scale,
                                        scale));
                            }
                        }
                    }
                }

                int targetX = position.x();
                int targetY = position.y();
                targetX += vx;
                for (RectangleHitbox obstacle : obstacles) {
                    if (hitbox.translate(targetX, targetY).intersects(obstacle)) {
                        if (vx > 0) {
                            int diff = targetX + hitbox.maxX() - obstacle.minX();
                            targetX -= diff;
                            collideX = true;
                        } else if (vx < 0) {
                            int diff = obstacle.maxX() - (targetX + hitbox.minX());
                            targetX += diff;
                            collideX = true;
                        }
                    }
                }
                targetY += vy;
                for (RectangleHitbox obstacle : obstacles) {
                    if (hitbox.translate(targetX, targetY).intersects(obstacle)) {
                        if (vy > 0) {
                            int diff = targetY + hitbox.maxY() - obstacle.minY();
                            targetY -= diff;
                            grounded = true;
                            collideY = true;
                        } else if (vy < 0) {
                            int diff = obstacle.maxY() - (targetY + hitbox.minY());
                            targetY += diff;
                            collideY = true;
                        }
                    }
                }
                for (int other : data.list(Obstaclebox.class)) {
                    if (entity == other) {
                        continue;
                    }
                    Position otherPos = data.get(other, Position.class);
                    Obstaclebox obstaclebox = data.get(other, Obstaclebox.class);

                    RectangleHitbox obstacle = obstaclebox.hitbox().translate(otherPos.x(), otherPos.y());
                    if (hitbox.translate(targetX, targetY).intersects(obstacle)) {
                        if (vy > 0) {
                            int diff = targetY + hitbox.maxY() - obstacle.minY();
                            targetY -= diff;
                            grounded = true;
                            collideY = true;
                            data.set(entity, new OnPlatform(other));
                        } else if (vy < 0) {
                            int diff = obstacle.maxY() - (targetY + hitbox.minY());
                            targetY += diff;
                            collideY = true;
                        }
                    }
                }

                if (collideX) {
                    vx = 0;
                }
                if (collideY) {
                    vy = 0;
                }
                if (grounded) {
                    data.set(entity, new OnGround());
                } else {
                    data.remove(entity, OnGround.class);
                }
                data.set(entity, new Speed(vx, vy));
                data.set(entity, new Position(targetX, targetY));
            } else {
                data.set(entity, position.translate(speed.x(), speed.y()));
            }
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
