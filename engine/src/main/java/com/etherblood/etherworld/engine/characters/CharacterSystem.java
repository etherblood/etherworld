package com.etherblood.etherworld.engine.characters;

import com.etherblood.etherworld.data.EntityData;
import com.etherblood.etherworld.engine.EntityUtil;
import com.etherblood.etherworld.engine.Etherworld;
import com.etherblood.etherworld.engine.PlayerAction;
import com.etherblood.etherworld.engine.RectangleHitbox;
import com.etherblood.etherworld.engine.characters.components.AttackParams;
import com.etherblood.etherworld.engine.characters.components.CharacterStateKey;
import com.etherblood.etherworld.engine.characters.components.PhysicParams;
import com.etherblood.etherworld.engine.chunks.Chunk;
import com.etherblood.etherworld.engine.chunks.ChunkManager;
import com.etherblood.etherworld.engine.chunks.ChunkPosition;
import com.etherblood.etherworld.engine.chunks.LocalTilePosition;
import com.etherblood.etherworld.engine.chunks.TilePosition;
import com.etherblood.etherworld.engine.components.Attackbox;
import com.etherblood.etherworld.engine.components.FacingDirection;
import com.etherblood.etherworld.engine.components.GameCharacter;
import com.etherblood.etherworld.engine.components.Health;
import com.etherblood.etherworld.engine.components.Hurtbox;
import com.etherblood.etherworld.engine.components.Movebox;
import com.etherblood.etherworld.engine.components.Obstaclebox;
import com.etherblood.etherworld.engine.components.OnGround;
import com.etherblood.etherworld.engine.components.OwnerId;
import com.etherblood.etherworld.engine.components.Position;
import com.etherblood.etherworld.engine.components.Respawn;
import com.etherblood.etherworld.engine.components.Speed;
import com.etherblood.etherworld.engine.systems.GameSystem;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

public class CharacterSystem implements GameSystem {

    private final Map<String, CharacterParams> characterParams;

    public CharacterSystem(Map<String, CharacterParams> characterParams) {
        this.characterParams = characterParams;
    }

    @Override
    public void tick(Etherworld world, Map<Integer, Set<PlayerAction>> playerActions) {
        EntityData data = world.getData();
        for (int entity : data.list(CharacterStateKey.class)) {
            data.remove(entity, Attackbox.class);
            GameCharacter gameCharacter = data.get(entity, GameCharacter.class);

            try {
                CharacterParams params = characterParams.get(gameCharacter.id());
                CharacterStateKey stateKey = data.get(entity, CharacterStateKey.class);
                updateCharacter(world, playerActions, params, entity, stateKey.value(), (int) (world.getTick() - stateKey.startTick()));
            } catch (Throwable t) {
                throw new RuntimeException("Failed to update entity with character " + gameCharacter, t);
            }
        }
    }


    private void updateCharacter(Etherworld world, Map<Integer, Set<PlayerAction>> playerActions, CharacterParams params, int entity, CharacterState state, int elapsedTicks) {
        EntityData data = world.getData();

        // expire previous state
        if (state == CharacterState.ATTACK) {
            AttackParams attackParams = params.attack();
            if (elapsedTicks >= attackParams.attackTicks()) {
                data.remove(entity, Attackbox.class);
                data.set(entity, new CharacterStateKey(CharacterState.IDLE, world.getTick()));
                state = CharacterState.IDLE;
            }
        } else if (state == CharacterState.DEAD) {
            if (elapsedTicks >= params.hurt().respawnTicks()) {
                Respawn respawn = data.get(entity, Respawn.class);
                if (respawn != null) {
                    data.set(entity, respawn.position());
                    data.set(entity, new Speed(0, 0));
                    data.set(entity, new Movebox(params.physics().hitbox()));
                    data.set(entity, new Hurtbox(params.physics().hitbox()));
                    Health health = data.get(entity, Health.class);
                    if (health != null) {
                        data.set(entity, new Health(health.max(), health.max()));
                    }
                    data.set(entity, new CharacterStateKey(CharacterState.IDLE, world.getTick()));
                    state = CharacterState.IDLE;
                }
            }
        } else if (state == CharacterState.HURT) {
            if (elapsedTicks >= params.hurt().hurtTicks()) {
                data.set(entity, new CharacterStateKey(CharacterState.IDLE, world.getTick()));
                state = CharacterState.IDLE;
            }
        }


        if (state != CharacterState.HURT && state != CharacterState.DEAD) {
            boolean crouchAction = false;
            if (params.crouch() != null) {
                OwnerId owner = data.get(entity, OwnerId.class);
                if (owner != null) {
                    Set<PlayerAction> actions = playerActions.getOrDefault(owner.id(), Collections.emptySet());
                    if (actions.contains(PlayerAction.CROUCH)) {
                        crouchAction = true;
                    }
                }
            }

            if (crouchAction) {
                if (state != CharacterState.CROUCH) {
                    data.set(entity, new CharacterStateKey(CharacterState.CROUCH, world.getTick()));
                    state = CharacterState.CROUCH;
                    data.set(entity, new Movebox(params.crouch().hitbox()));
                    data.set(entity, new Hurtbox(params.crouch().hitbox()));
                }
            } else {
                if (state == CharacterState.CROUCH) {
                    Position position = data.get(entity, Position.class);
                    if (!intersectsWorld(world, params.physics().hitbox().translate(position), entity)) {
                        // TODO: above check should ignore obstacles that already collide while crouching
                        data.set(entity, new CharacterStateKey(CharacterState.IDLE, world.getTick()));
                        state = CharacterState.IDLE;
                        data.set(entity, new Movebox(params.physics().hitbox()));
                        data.set(entity, new Hurtbox(params.physics().hitbox()));
                    }
                }
            }

            // damage hitbox check after crouch
            Map<Integer, Attackbox> attacks = EntityUtil.findAttacks(data, entity);
            int damage = attacks.values().stream().mapToInt(Attackbox::damage).sum();
            if (damage > 0) {
                Health health = data.get(entity, Health.class);
                if (health != null) {
                    data.set(entity, health.damage(damage));
                }
                data.set(entity, new CharacterStateKey(CharacterState.HURT, world.getTick()));
                return;
            }
        }

        if (state != CharacterState.DEAD) {
            Health health = data.get(entity, Health.class);
            if (health != null && health.value() <= 0) {
                data.set(entity, new CharacterStateKey(CharacterState.DEAD, world.getTick()));
                data.remove(entity, Hurtbox.class);
            }
        }

        // apply selected state
        applyPhysics(world, state != CharacterState.DEAD ? playerActions : null, params, entity, state, data);

        if (state == CharacterState.ATTACK) {
            AttackParams attackParams = params.attack();
            if (attackParams.damageStart() == elapsedTicks) {
                data.set(entity, new Attackbox(attackParams.damageBox(), attackParams.damage()));
            }
            if (elapsedTicks == attackParams.damageEnd()) {
                data.remove(entity, Attackbox.class);
            }
        }
    }

    private boolean intersectsWorld(Etherworld world, RectangleHitbox hitbox, int excludeEntity) {
        ChunkManager chunks = world.getChunks();
        TilePosition minTile = chunks.toFloorTilePosition(new Position(hitbox.minX(), hitbox.minY()));
        TilePosition maxTile = chunks.toCeilTilePosition(new Position(hitbox.maxX(), hitbox.maxY()));
        for (int y = minTile.y(); y < maxTile.y(); y++) {
            for (int x = minTile.x(); x < maxTile.x(); x++) {
                TilePosition tilePosition = new TilePosition(x, y);
                ChunkPosition chunkPosition = chunks.toChunkPosition(tilePosition);
                Chunk chunk = chunks.get(chunkPosition);
                if (chunk != null) {
                    LocalTilePosition tile = chunks.toLocalChunkPosition(tilePosition);
                    if (chunk.isObstacle(tile)) {
                        return true;
                    }
                }
            }
        }

        EntityData data = world.getData();
        for (int other : data.list(Obstaclebox.class)) {
            if (other == excludeEntity) {
                continue;
            }
            Obstaclebox obstaclebox = data.get(other, Obstaclebox.class);
            Position position = data.get(other, Position.class);
            RectangleHitbox obstacle = obstaclebox.hitbox().translate(position);
            if (obstacle.intersects(hitbox)) {
                return true;
            }
        }
        return false;
    }

    private void applyPhysics(Etherworld world, Map<Integer, Set<PlayerAction>> playerActions, CharacterParams params, int entity, CharacterState state, EntityData data) {
        Speed speed = data.get(entity, Speed.class);
        if (speed == null) {
            speed = new Speed(0, 0);
        }
        int vx = 0;
        int vy = speed.y() + params.physics().gravityPerTick();

        Speed nextSpeed;
        OwnerId owner = data.get(entity, OwnerId.class);
        if (playerActions != null && owner != null) {
            Set<PlayerAction> actions = playerActions.getOrDefault(owner.id(), Collections.emptySet());
            nextSpeed = applyPlayerActions(world, entity, (state == CharacterState.IDLE && params.attack() != null) ? CharacterState.ATTACK : null, state == CharacterState.IDLE || state == CharacterState.CROUCH, data, params.physics(), vx, vy, actions);
        } else {
            nextSpeed = new Speed(vx, vy);
        }
        data.set(entity, nextSpeed);
    }

    private Speed applyPlayerActions(Etherworld world, int entity, CharacterState onAttack, boolean canTurn, EntityData data, PhysicParams physicParams, int vx, int vy, Set<PlayerAction> actions) {
        if (onAttack != null && actions.contains(PlayerAction.ATTACK)) {
            data.set(entity, new CharacterStateKey(onAttack, world.getTick()));
        }
        if (actions.contains(PlayerAction.RIGHT) && !actions.contains(PlayerAction.LEFT)) {
            if (canTurn) {
                data.set(entity, FacingDirection.RIGHT);
            }
            vx += physicParams.runSpeed();
        }
        if (actions.contains(PlayerAction.LEFT) && !actions.contains(PlayerAction.RIGHT)) {
            if (canTurn) {
                data.set(entity, FacingDirection.LEFT);
            }
            vx -= physicParams.runSpeed();
        }
        if (actions.contains(PlayerAction.JUMP)) {
            if (data.has(entity, OnGround.class)) {
                vy = -physicParams.jumpStrength();
            } else {
                vy -= physicParams.hoverStrength();
            }
        }
        return new Speed(vx, vy);
    }
}
