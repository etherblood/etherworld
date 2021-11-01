package com.etherblood.etherworld.engine.characters;

import com.etherblood.etherworld.data.EntityData;
import com.etherblood.etherworld.engine.EntityUtil;
import com.etherblood.etherworld.engine.Etherworld;
import com.etherblood.etherworld.engine.PlayerAction;
import com.etherblood.etherworld.engine.characters.components.AttackParams;
import com.etherblood.etherworld.engine.characters.components.CharacterStateKey;
import com.etherblood.etherworld.engine.characters.components.HurtParams;
import com.etherblood.etherworld.engine.characters.components.PhysicParams;
import com.etherblood.etherworld.engine.components.Attackbox;
import com.etherblood.etherworld.engine.components.FacingDirection;
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

    @Override
    public void tick(Etherworld world, Map<Integer, Set<PlayerAction>> playerActions) {
        // TODO: we effectively skip the first frame of every state, due to setting the next state at the end of frames

        EntityData data = world.getData();
        for (int entity : data.list(CharacterStateKey.class)) {
            data.remove(entity, Attackbox.class);

            CharacterStateKey stateKey = data.get(entity, CharacterStateKey.class);
            switch (stateKey.value()) {
                case IDLE, ATTACK -> normal(world, playerActions, entity, stateKey.value(), (int) (world.getTick() - stateKey.startTick()));
                case HURT -> hurt(world, playerActions, entity, (int) (world.getTick() - stateKey.startTick()));
                case DEAD -> dead(world, playerActions, entity, (int) (world.getTick() - stateKey.startTick()));
                default -> throw new AssertionError(stateKey.value());
            }
        }
    }

    private void normal(Etherworld world, Map<Integer, Set<PlayerAction>> playerActions, int entity, CharacterState state, int elapsedTicks) {
        EntityData data = world.getData();
        PhysicParams physicParams = data.get(entity, PhysicParams.class);
        Speed speed = data.get(entity, Speed.class);
        if (speed == null) {
            speed = new Speed(0, 0);
        }
        int vx = 0;
        int vy = speed.y() + physicParams.gravityPerTick();

        OwnerId owner = data.get(entity, OwnerId.class);
        if (owner != null) {
            if (state != CharacterState.ATTACK && playerActions.get(owner.id()).contains(PlayerAction.ATTACK)) {
                data.set(entity, new CharacterStateKey(CharacterState.ATTACK, world.getTick()));
            }
            Set<PlayerAction> actions = playerActions.getOrDefault(owner.id(), Collections.emptySet());
            if (actions.contains(PlayerAction.RIGHT) && !actions.contains(PlayerAction.LEFT)) {
                if (state != CharacterState.ATTACK) {
                    data.set(entity, FacingDirection.RIGHT);
                }
                vx += physicParams.runSpeed();
            }
            if (actions.contains(PlayerAction.LEFT) && !actions.contains(PlayerAction.RIGHT)) {
                if (state != CharacterState.ATTACK) {
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
        }
        data.set(entity, new Speed(vx, vy));

        Map<Integer, Attackbox> attacks = EntityUtil.findAttacks(data, entity);
        int damage = attacks.values().stream().mapToInt(Attackbox::damage).sum();
        if (damage > 0) {
            data.set(entity, new CharacterStateKey(CharacterState.HURT, world.getTick()));
            Health health = data.get(entity, Health.class);
            if (health != null) {
                data.set(entity, health.damage(damage));
            }
        } else if (state == CharacterState.ATTACK) {
            AttackParams attackParams = data.get(entity, AttackParams.class);
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

    private void hurt(Etherworld world, Map<Integer, Set<PlayerAction>> playerActions, int entity, int elapsedTicks) {
        EntityData data = world.getData();
        PhysicParams physicParams = data.get(entity, PhysicParams.class);
        Speed speed = data.get(entity, Speed.class);
        if (speed != null) {
            data.set(entity, new Speed(0, speed.y() + physicParams.gravityPerTick()));
        } else {
            data.set(entity, new Speed(0, physicParams.gravityPerTick()));
        }
        HurtParams hurtParams = data.get(entity, HurtParams.class);
        if (elapsedTicks >= hurtParams.hurtTicks()) {
            data.set(entity, new CharacterStateKey(CharacterState.IDLE, world.getTick()));
        }
    }

    private void dead(Etherworld world, Map<Integer, Set<PlayerAction>> playerActions, int entity, int elapsedTicks) {
        EntityData data = world.getData();
        PhysicParams physicParams = data.get(entity, PhysicParams.class);
        HurtParams hurtParams = data.get(entity, HurtParams.class);
        Speed speed = data.get(entity, Speed.class);
        if (speed != null) {
            data.set(entity, new Speed(0, speed.y() + physicParams.gravityPerTick()));
        } else {
            data.set(entity, new Speed(0, physicParams.gravityPerTick()));
        }
        if (elapsedTicks >= hurtParams.respawnTicks()) {
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
        }
    }
}
