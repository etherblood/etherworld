package com.etherblood.etherworld.engine.characters;

import com.etherblood.etherworld.data.EntityData;
import com.etherblood.etherworld.engine.EntityUtil;
import com.etherblood.etherworld.engine.Etherworld;
import com.etherblood.etherworld.engine.PlayerAction;
import com.etherblood.etherworld.engine.characters.components.AttackParams;
import com.etherblood.etherworld.engine.characters.components.CharacterStateKey;
import com.etherblood.etherworld.engine.characters.components.PhysicParams;
import com.etherblood.etherworld.engine.components.Attackbox;
import com.etherblood.etherworld.engine.components.FacingDirection;
import com.etherblood.etherworld.engine.components.GameCharacter;
import com.etherblood.etherworld.engine.components.Health;
import com.etherblood.etherworld.engine.components.Hurtbox;
import com.etherblood.etherworld.engine.components.Movebox;
import com.etherblood.etherworld.engine.components.OnGround;
import com.etherblood.etherworld.engine.components.OwnerId;
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
                switch (stateKey.value()) {
                    case IDLE, ATTACK -> normal(world, playerActions, params, entity, stateKey.value(), (int) (world.getTick() - stateKey.startTick()));
                    case HURT -> hurt(world, playerActions, params, entity, (int) (world.getTick() - stateKey.startTick()));
                    case DEAD -> dead(world, playerActions, params, entity, (int) (world.getTick() - stateKey.startTick()));
                    default -> throw new AssertionError(stateKey.value());
                }
            } catch (Throwable t) {
                throw new RuntimeException("Failed to update entity with character " + gameCharacter, t);
            }
        }
    }

    private void normal(Etherworld world, Map<Integer, Set<PlayerAction>> playerActions, CharacterParams params, int entity, CharacterState state, int elapsedTicks) {
        EntityData data = world.getData();
        applyPhysics(world, playerActions, params, entity, state, data);

        Map<Integer, Attackbox> attacks = EntityUtil.findAttacks(data, entity);
        int damage = attacks.values().stream().mapToInt(Attackbox::damage).sum();
        if (damage > 0) {
            data.set(entity, new CharacterStateKey(CharacterState.HURT, world.getTick()));
            Health health = data.get(entity, Health.class);
            if (health != null) {
                data.set(entity, health.damage(damage));
            }
        } else if (state == CharacterState.ATTACK) {
            AttackParams attackParams = params.attack();
            if (attackParams.damageFrom() <= elapsedTicks && elapsedTicks <= attackParams.damageTo()) {
                data.set(entity, new Attackbox(attackParams.damageBox(), attackParams.damage()));
            }
            if (elapsedTicks >= attackParams.attackTicks()) {
                data.set(entity, new CharacterStateKey(CharacterState.IDLE, world.getTick()));
            }
        }

        Health health = data.get(entity, Health.class);
        if (health != null && health.value() <= 0) {
            data.set(entity, new CharacterStateKey(CharacterState.DEAD, world.getTick()));
            data.remove(entity, Hurtbox.class);
        }
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
            nextSpeed = applyPlayerActions(world, entity, (state == CharacterState.IDLE && params.attack() != null) ? CharacterState.ATTACK : null, state == CharacterState.IDLE, data, params.physics(), vx, vy, actions);
        } else {
            nextSpeed = new Speed(vx, vy);
        }
        data.set(entity, nextSpeed);
    }

    private Speed applyPlayerActions(Etherworld world, int entity, CharacterState onAttack, boolean canTurn, EntityData data, PhysicParams physicParams, int vx, int vy, Set<PlayerAction> actions) {
        if (onAttack != null && actions.contains(PlayerAction.ATTACK)) {
            data.set(entity, new CharacterStateKey(CharacterState.ATTACK, world.getTick()));
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

    private void hurt(Etherworld world, Map<Integer, Set<PlayerAction>> playerActions, CharacterParams params, int entity, int elapsedTicks) {
        EntityData data = world.getData();
        applyPhysics(world, null, params, entity, CharacterState.HURT, data);
        if (elapsedTicks >= params.hurt().hurtTicks()) {
            data.set(entity, new CharacterStateKey(CharacterState.IDLE, world.getTick()));
        }
    }

    private void dead(Etherworld world, Map<Integer, Set<PlayerAction>> playerActions, CharacterParams params, int entity, int elapsedTicks) {
        EntityData data = world.getData();
        if (elapsedTicks >= params.hurt().respawnTicks()) {
            Respawn respawn = data.get(entity, Respawn.class);
            if (respawn != null) {
                data.set(entity, respawn.position());
                data.set(entity, new Speed(0, 0));
                data.set(entity, new CharacterStateKey(CharacterState.IDLE, world.getTick()));
                data.set(entity, new Hurtbox(data.get(entity, Movebox.class).hitbox()));
                Health health = data.get(entity, Health.class);
                if (health != null) {
                    data.set(entity, new Health(health.max(), health.max()));
                }
            }
        } else {
            applyPhysics(world, null, params, entity, CharacterState.HURT, data);
        }
    }
}
