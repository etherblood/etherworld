package com.etherblood.etherworld.engine;

import com.etherblood.etherworld.data.EntityData;
import com.etherblood.etherworld.engine.chunks.Chunk;
import com.etherblood.etherworld.engine.chunks.ChunkManager;
import com.etherblood.etherworld.engine.chunks.ChunkPosition;
import com.etherblood.etherworld.engine.chunks.LocalTilePosition;
import com.etherblood.etherworld.engine.chunks.TilePosition;
import com.etherblood.etherworld.engine.components.Animation;
import com.etherblood.etherworld.engine.components.CharacterId;
import com.etherblood.etherworld.engine.components.Direction;
import com.etherblood.etherworld.engine.components.OnGround;
import com.etherblood.etherworld.engine.components.OwnerId;
import com.etherblood.etherworld.engine.components.Position;
import com.etherblood.etherworld.engine.components.Speed;
import com.etherblood.etherworld.engine.sprites.GameSprite;
import com.etherblood.etherworld.engine.sprites.GameSpriteAnimation;
import com.etherblood.etherworld.engine.sprites.GameSpriteHitbox;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Etherworld {

    private final EntityData data;
    private final Map<String, GameSprite> sprites;
    private final PositionConverter converter;
    private final ChunkManager chunks;

    public Etherworld(EntityData data, Map<String, GameSprite> sprites, PositionConverter converter, ChunkManager chunks) {
        this.data = data;
        this.sprites = sprites;
        this.converter = converter;
        this.chunks = chunks;
    }

    public void tick(Map<Integer, Set<PlayerAction>> playerActions) {
        int gravityPerTick = 12;
        int jumpStrength = 16 * 16;
        int runSpeed = 8 * 16;

        for (int entity : data.list(Speed.class)) {
            Speed speed = data.get(entity, Speed.class);
            Position position = data.get(entity, Position.class);
            CharacterId characterId = data.get(entity, CharacterId.class);
            if (characterId != null) {
                GameSprite gameSprite = sprites.get(characterId.id());
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
//                    if (actions.contains(PlayerAction.ATTACK) && !"Attack".equals(character.state)) {
//                        character.state = "Attack";
//                        character.animationStartNanos = System.nanoTime();
//                    }
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


        for (int entity : data.list(Animation.class)) {
            Animation animation = data.get(entity, Animation.class);
            GameSprite sprite = sprites.get(animation.spriteId());
            GameSpriteAnimation spriteAnimation = sprite.animations().get(animation.animationId());

            int ticks = animation.elapsedTicks() + 1;

            // update state
            boolean fallBackState = true;
            String nextAnimation = animation.animationId();
            switch (animation.animationId()) {
                case "Attack":
                case "Hit":
                    if (ticks < spriteAnimation.totalTicks()) {
                        fallBackState = false;
                    }
                    break;
                default:
                    break;
            }
            if (fallBackState) {
                Speed speed = data.get(entity, Speed.class);
                if (speed == null) {
                    speed = new Speed(0, 0);
                }
                if (speed.x() < 0) {
                    data.set(entity, Direction.LEFT);
                } else if (speed.x() > 0) {
                    data.set(entity, Direction.RIGHT);
                }
                if (data.has(entity, OnGround.class)) {
                    if (speed.x() == 0) {
                        nextAnimation = "Stand";
                    } else {
                        nextAnimation = "Run";
                    }
                } else {
                    if (speed.y() < 0) {
                        nextAnimation = "Up";
                    } else {
                        nextAnimation = "Down";
                    }
                }
            }
            if (nextAnimation.equals(animation.animationId())) {
                data.set(entity, new Animation(
                        animation.spriteId(),
                        animation.animationId(),
                        ticks % spriteAnimation.totalTicks()));
            } else {
                data.set(entity, new Animation(
                        animation.spriteId(),
                        nextAnimation,
                        0));
            }
        }
    }

    public EntityData getData() {
        return data;
    }

    public Map<String, GameSprite> getSprites() {
        return sprites;
    }

    public PositionConverter getConverter() {
        return converter;
    }
}
