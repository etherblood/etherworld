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
import com.etherblood.etherworld.engine.collision.AxisDirection;
import com.etherblood.etherworld.engine.collision.Body;
import com.etherblood.etherworld.engine.collision.Collision;
import com.etherblood.etherworld.engine.collision.CollisionEngine;
import com.etherblood.etherworld.engine.components.Movebox;
import com.etherblood.etherworld.engine.components.Obstaclebox;
import com.etherblood.etherworld.engine.components.OnGround;
import com.etherblood.etherworld.engine.components.OnPlatform;
import com.etherblood.etherworld.engine.components.Position;
import com.etherblood.etherworld.engine.components.Speed;
import com.etherblood.etherworld.engine.math.Fraction;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class MovementSystem implements GameSystem {
    @Override
    public void tick(Etherworld world, Map<Integer, Set<PlayerAction>> playerActions) {
        Map<Integer, Speed> speeds = new LinkedHashMap<>();

        EntityData data = world.getData();
        for (int entity : data.list(Speed.class)) {
            Speed speed = data.get(entity, Speed.class);
            OnPlatform onPlatform = data.get(entity, OnPlatform.class);
            if (onPlatform != null) {
                speed = speed.add(data.get(onPlatform.platform(), Speed.class));
            }
            speeds.put(entity, speed);
        }

        List<Body> bodies = new ArrayList<>();
        for (int entity : data.list(Movebox.class)) {
            Body body = new Body();
            body.simulatedTime = Fraction.ofInt(0);
            body.speed = speeds.getOrDefault(entity, new Speed(0, 0));
            body.setPosition(data.get(entity, Position.class));
            body.id = entity;
            body.hitbox = data.get(entity, Movebox.class).hitbox();
            bodies.add(body);
        }
        List<Body> obstacles = new ArrayList<>();
        for (int entity : data.list(Obstaclebox.class)) {
            Body body = bodies.stream().filter(other -> other.id == entity).findAny().orElse(null);
            if (body == null) {
                body = new Body();
                body.simulatedTime = Fraction.ofInt(0);
                body.speed = speeds.getOrDefault(entity, new Speed(0, 0));
                body.setPosition(data.get(entity, Position.class));
                body.id = entity;
                body.hitbox = data.get(entity, Obstaclebox.class).hitbox();
            }
            obstacles.add(body);
        }

        CollisionEngine engine = new CollisionEngine();
        Map<Integer, List<Collision>> collisions = engine.tick(bodies, body -> obstacles, body -> findStaticObstacles(world, body));

        for (int entity : data.list(Movebox.class)) {
            data.remove(entity, OnGround.class);
            data.remove(entity, OnPlatform.class);
            for (Collision collision : collisions.getOrDefault(entity, Collections.emptyList())) {
                if (collision.normal() == AxisDirection.Y_POSITIVE) {
                    if (collision.b().id != null) {
                        data.set(entity, new OnPlatform(collision.b().id));
                    }
                    data.set(entity, new OnGround());
                    break;
                }
            }
        }

        for (Body body : bodies) {
            data.set(body.id, body.speed);
            data.set(body.id, new Position(body.simulatedPositionX.floorToInt(), body.simulatedPositionY.floorToInt()));
        }
        for (int entity : data.list(Speed.class)) {
            if (data.has(entity, Movebox.class)) {
                continue;
            }
            Speed speed = data.get(entity, Speed.class);
            data.set(entity, data.get(entity, Position.class).translate(speed.x(), speed.y()));
        }
        for (int entity : data.list(OnPlatform.class)) {
            int platform = data.get(entity, OnPlatform.class).platform();
            data.set(entity, data.get(entity, Speed.class).subtract(data.get(platform, Speed.class)));
        }
    }

    private static List<Body> findStaticObstacles(Etherworld world, RectangleHitbox bounds) {
        List<Body> result = new ArrayList<>();

        ChunkManager chunks = world.getChunks();
        TilePosition minTile = chunks.toFloorTilePosition(new Position(bounds.minX(), bounds.minY()));
        TilePosition maxTile = chunks.toCeilTilePosition(new Position(bounds.maxX(), bounds.maxY()));
        for (int y = minTile.y(); y < maxTile.y(); y++) {
            for (int x = minTile.x(); x < maxTile.x(); x++) {
                TilePosition tilePosition = new TilePosition(x, y);
                ChunkPosition chunkPosition = chunks.toChunkPosition(tilePosition);
                Chunk chunk = chunks.get(chunkPosition);
                if (chunk != null) {
                    LocalTilePosition tile = chunks.toLocalChunkPosition(tilePosition);
                    if (chunk.isObstacle(tile)) {
                        int scale = chunks.getTileSize();
                        Body chunkBody = new Body();
                        chunkBody.simulatedTime = Fraction.ofInt(0);
                        chunkBody.speed = new Speed(0, 0);
                        chunkBody.setPosition(new Position(0, 0));
                        chunkBody.id = null;
                        chunkBody.hitbox = new RectangleHitbox(x * scale, y * scale, scale, scale);
                        result.add(chunkBody);
                    }
                }
            }
        }
        return result;
    }
}
