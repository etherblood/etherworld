package com.etherblood.etherworld.engine.characters;

import com.etherblood.etherworld.data.EntityData;
import com.etherblood.etherworld.engine.EntityState;
import com.etherblood.etherworld.engine.Etherworld;
import com.etherblood.etherworld.engine.PlayerAction;
import com.etherblood.etherworld.engine.RectangleHitbox;
import com.etherblood.etherworld.engine.components.CharacterState;
import com.etherblood.etherworld.engine.components.FacingDirection;
import com.etherblood.etherworld.engine.components.Health;
import com.etherblood.etherworld.engine.components.Hurtbox;
import com.etherblood.etherworld.engine.components.OnGround;
import com.etherblood.etherworld.engine.components.OwnerId;
import com.etherblood.etherworld.engine.components.Position;
import com.etherblood.etherworld.engine.components.Speed;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

public class AttackState implements State {

    private final PhysicParams physicParams;
    private final AttackParams attackParams;

    public AttackState(PhysicParams physicParams, AttackParams attackParams) {
        this.physicParams = physicParams;
        this.attackParams = attackParams;
    }

    @Override
    public void tick(Etherworld world, Map<Integer, Set<PlayerAction>> playerActions, int entity, int elapsedTicks) {
        EntityData data = world.getData();
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

        if (attackParams.damageFrom() <= elapsedTicks && elapsedTicks <= attackParams.damageTo()) {
            Position position = data.get(entity, Position.class);
            RectangleHitbox attackbox = attackParams.damageBox().translate(position.x(), position.y());
            FacingDirection direction = data.get(entity, FacingDirection.class);
            if (direction == FacingDirection.LEFT) {
                attackbox = attackbox.mirrorX(position.x());
            }
            for (int other : data.list(Hurtbox.class)) {
                if (entity == other) {
                    continue;
                }
                Position otherPosition = data.get(other, Position.class);
                RectangleHitbox hurtbox = data.get(other, Hurtbox.class).hitbox().translate(otherPosition.x(), otherPosition.y());
                if (data.get(other, FacingDirection.class) == FacingDirection.LEFT) {
                    hurtbox = hurtbox.mirrorX(otherPosition.x());
                }
                if (attackbox.intersects(hurtbox)) {
                    CharacterState otherAnimation = data.get(other, CharacterState.class);
                    if (otherAnimation.value() != EntityState.HURT && otherAnimation.value() != EntityState.DEAD) {
                        data.set(other, new CharacterState(EntityState.HURT, world.getTick()));
                        Health health = data.get(other, Health.class);
                        if (health != null) {
                            data.set(other, health.damage(attackParams.damage()));
                        }
                    }
                }
            }
        }
        if (elapsedTicks >= attackParams.attackTicks()) {
            data.set(entity, new CharacterState(EntityState.IDLE, world.getTick()));
        }
    }
}
