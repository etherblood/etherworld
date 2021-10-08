package com.etherblood.etherworld.engine.systems;

import com.etherblood.etherworld.data.EntityData;
import com.etherblood.etherworld.engine.Etherworld;
import com.etherblood.etherworld.engine.PlayerAction;
import com.etherblood.etherworld.engine.PositionConverter;
import com.etherblood.etherworld.engine.chunks.Chunk;
import com.etherblood.etherworld.engine.chunks.ChunkManager;
import com.etherblood.etherworld.engine.chunks.ChunkPosition;
import com.etherblood.etherworld.engine.chunks.LocalTilePosition;
import com.etherblood.etherworld.engine.chunks.TilePosition;
import com.etherblood.etherworld.engine.components.CharacterId;
import com.etherblood.etherworld.engine.components.OnGround;
import com.etherblood.etherworld.engine.components.OwnerId;
import com.etherblood.etherworld.engine.components.Position;
import com.etherblood.etherworld.engine.components.Speed;
import com.etherblood.etherworld.engine.sprites.GameSprite;
import com.etherblood.etherworld.engine.sprites.GameSpriteHitbox;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

public class MoveSystem implements GameSystem {

    @Override
    public void tick(Etherworld world, Map<Integer, Set<PlayerAction>> playerActions) {
        int gravityPerTick = 12;
        int jumpStrength = 16 * 16;
        int runSpeed = 8 * 16;

        EntityData data = world.getData();
        PositionConverter converter = world.getConverter();
        Function<String, GameSprite> sprites = world.getSprites();
        ChunkManager chunks = world.getChunks();

        for (int entity : data.list(Speed.class)) {
            Speed speed = data.get(entity, Speed.class);
            Position position = data.get(entity, Position.class);
            CharacterId characterId = data.get(entity, CharacterId.class);
            if (characterId != null) {
                GameSprite gameSprite = sprites.apply(characterId.id());
                GameSpriteHitbox hitbox = gameSprite.hitbox();

                int vx = 0;
                int vy = speed.y();
                OwnerId owner = data.get(entity, OwnerId.class);
                if (owner != null) {
                    Set<PlayerAction> actions = playerActions.getOrDefault(owner.id(), Collections.emptySet());
                    if (actions.contains(PlayerAction.RIGHT)) {
                        vx += runSpeed;
                    }
                    if (actions.contains(PlayerAction.LEFT)) {
                        vx -= runSpeed;
                    }
                    if (actions.contains(PlayerAction.JUMP) && data.has(entity, OnGround.class)) {
                        vy = -jumpStrength;
                    }
                }

                boolean grounded = false;
                vy += gravityPerTick;


                boolean collideX = false;
                boolean collideY = false;

                int roiMinX = Math.min(vx, 0) + position.x() + hitbox.minX();
                int roiMaxX = Math.max(vx, 0) + position.x() + hitbox.maxX();
                int roiMinY = Math.min(vy, 0) + position.y() + hitbox.minY();
                int roiMaxY = Math.max(vy, 0) + position.y() + hitbox.maxY();

                TilePosition minTile = converter.floorTile(new Position(roiMinX, roiMinY));
                TilePosition maxTile = converter.ceilTile(new Position(roiMaxX, roiMaxY));

                List<GameSpriteHitbox> obstacles = new ArrayList<>();
                for (int y = minTile.y(); y < maxTile.y(); y++) {
                    for (int x = minTile.x(); x < maxTile.x(); x++) {
                        TilePosition tilePosition = new TilePosition(x, y);
                        ChunkPosition chunkPosition = converter.floorChunk(tilePosition);
                        Chunk chunk = chunks.get(chunkPosition);
                        if (chunk != null) {
                            LocalTilePosition tile = converter.toLocal(tilePosition);
                            if (chunk.isObstacle(tile)) {
                                int scale = converter.getTileSize() * converter.getPixelSize();
                                obstacles.add(new GameSpriteHitbox(
                                        x * scale,
                                        y * scale,
                                        scale,
                                        scale));
                            }
                        }
                    }
                }
                //characterObstacles.put(character, obstacles);

                int targetX = position.x();
                int targetY = position.y();
                targetX += vx;
                for (GameSpriteHitbox obstacle : obstacles) {
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
                for (GameSpriteHitbox obstacle : obstacles) {
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
}
