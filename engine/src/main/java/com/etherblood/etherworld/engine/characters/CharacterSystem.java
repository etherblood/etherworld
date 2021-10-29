package com.etherblood.etherworld.engine.characters;

import com.etherblood.etherworld.data.EntityData;
import com.etherblood.etherworld.engine.EntityUtil;
import com.etherblood.etherworld.engine.Etherworld;
import com.etherblood.etherworld.engine.PlayerAction;
import com.etherblood.etherworld.engine.RectangleHitbox;
import com.etherblood.etherworld.engine.characters.components.AttackParams;
import com.etherblood.etherworld.engine.characters.components.CharacterStateKey;
import com.etherblood.etherworld.engine.characters.components.HurtParams;
import com.etherblood.etherworld.engine.characters.components.PhysicParams;
import com.etherblood.etherworld.engine.components.Attackbox;
import com.etherblood.etherworld.engine.components.FacingDirection;
import com.etherblood.etherworld.engine.components.Health;
import com.etherblood.etherworld.engine.components.Hurtbox;
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

    @Override
    public void tick(Etherworld world, Map<Integer, Set<PlayerAction>> playerActions) {
        EntityData data = world.getData();
        for (int entity : data.list(CharacterStateKey.class)) {
            CharacterStateKey stateKey = data.get(entity, CharacterStateKey.class);
            switch (stateKey.value()) {
                case IDLE -> idle(world, playerActions, entity, (int) (world.getTick() - stateKey.startTick()));
                case ATTACK -> attack(world, playerActions, entity, (int) (world.getTick() - stateKey.startTick()));
                case HURT -> hurt(world, playerActions, entity, (int) (world.getTick() - stateKey.startTick()));
                case DEAD -> dead(world, playerActions, entity, (int) (world.getTick() - stateKey.startTick()));
                default -> throw new AssertionError(stateKey.value());
            }
        }
    }

    private void idle(Etherworld world, Map<Integer, Set<PlayerAction>> playerActions, int entity, int elapsedTicks) {
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
            if (playerActions.get(owner.id()).contains(PlayerAction.ATTACK)) {
                data.set(entity, new CharacterStateKey(CharacterState.ATTACK, world.getTick()));
            }
            Set<PlayerAction> actions = playerActions.getOrDefault(owner.id(), Collections.emptySet());
            if (actions.contains(PlayerAction.RIGHT) && !actions.contains(PlayerAction.LEFT)) {
                data.set(entity, FacingDirection.RIGHT);
                vx += physicParams.runSpeed();
            }
            if (actions.contains(PlayerAction.LEFT) && !actions.contains(PlayerAction.RIGHT)) {
                data.set(entity, FacingDirection.LEFT);
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
        }

        Health health = data.get(entity, Health.class);
        if (health != null && health.value() <= 0) {
            data.set(entity, new CharacterStateKey(CharacterState.DEAD, world.getTick()));
        }
    }


    private void attack(Etherworld world, Map<Integer, Set<PlayerAction>> playerActions, int entity, int elapsedTicks) {
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
            Set<PlayerAction> actions = playerActions.getOrDefault(owner.id(), Collections.emptySet());
            if (actions.contains(PlayerAction.RIGHT)) {
                vx += physicParams.runSpeed();
            }
            if (actions.contains(PlayerAction.LEFT)) {
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

        boolean cancelled = false;
        Hurtbox hurtbox = data.get(entity, Hurtbox.class);
        if (hurtbox != null) {
            RectangleHitbox hurtHitbox = hurtbox.hitbox();
            Position position = data.get(entity, Position.class);
            if (data.get(entity, FacingDirection.class) == FacingDirection.LEFT) {
                hurtHitbox = hurtHitbox.mirrorX(position.x());
            }
            for (int other : data.list(Attackbox.class)) {
                if (entity == other) {
                    continue;
                }
                Position otherPosition = data.get(other, Position.class);
                Attackbox attackbox = data.get(other, Attackbox.class);
                RectangleHitbox attackHitbox = attackbox.hitbox().translate(otherPosition);
                if (data.get(other, FacingDirection.class) == FacingDirection.LEFT) {
                    attackHitbox = attackHitbox.mirrorX(otherPosition.x());
                }
                if (attackHitbox.intersects(hurtHitbox)) {
                    EntityData data1 = world.getData();
                    data1.remove(entity, Attackbox.class);
                    data.set(entity, new CharacterStateKey(CharacterState.HURT, world.getTick()));
                    Health health = data.get(entity, Health.class);
                    if (health != null) {
                        data.set(entity, health.damage(attackbox.damage()));
                    }
                    cancelled = true;
                }
            }
        }

        if (!cancelled) {
            AttackParams attackParams = data.get(entity, AttackParams.class);
            if (attackParams.damageFrom() == elapsedTicks) {
                data.set(entity, new Attackbox(attackParams.damageBox(), attackParams.damage()));
            }
            if (elapsedTicks == attackParams.damageTo() + 1) {
                data.remove(entity, Attackbox.class);
            }
            if (elapsedTicks >= attackParams.attackTicks()) {
                data.remove(entity, Attackbox.class);
                data.set(entity, new CharacterStateKey(CharacterState.IDLE, world.getTick()));
            }
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
                Health health = data.get(entity, Health.class);
                if (health != null) {
                    data.set(entity, new Health(health.max(), health.max()));
                }
            }
        }
    }
}
