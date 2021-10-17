package com.etherblood.etherworld.engine.characters;

import com.etherblood.etherworld.data.EntityData;
import com.etherblood.etherworld.engine.Etherworld;
import com.etherblood.etherworld.engine.PlayerAction;
import com.etherblood.etherworld.engine.RectangleHitbox;
import com.etherblood.etherworld.engine.components.Attackbox;
import com.etherblood.etherworld.engine.components.BehaviourKey;
import com.etherblood.etherworld.engine.components.FacingDirection;
import com.etherblood.etherworld.engine.components.GameCharacter;
import com.etherblood.etherworld.engine.components.Health;
import com.etherblood.etherworld.engine.components.Hurtbox;
import com.etherblood.etherworld.engine.components.OnGround;
import com.etherblood.etherworld.engine.components.OwnerId;
import com.etherblood.etherworld.engine.components.Position;
import com.etherblood.etherworld.engine.components.Speed;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

public class AttackBehaviour implements Behaviour {

    private final String idleBehaviour;
    private final String hurtBehaviour;

    public AttackBehaviour(String idleBehaviour, String hurtBehaviour) {
        this.idleBehaviour = idleBehaviour;
        this.hurtBehaviour = hurtBehaviour;
    }

    @Override
    public void tick(Etherworld world, Map<Integer, Set<PlayerAction>> playerActions, int entity, int elapsedTicks) {
        EntityData data = world.getData();
        GameCharacter gameCharacter = data.get(entity, GameCharacter.class);
        PhysicParams physicParams = gameCharacter.physicParams();
        AttackParams attackParams = gameCharacter.attackParams();
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
                RectangleHitbox attackHitbox = attackbox.hitbox().translate(otherPosition.x(), otherPosition.y());
                if (data.get(other, FacingDirection.class) == FacingDirection.LEFT) {
                    attackHitbox = attackHitbox.mirrorX(otherPosition.x());
                }
                if (attackHitbox.intersects(hurtHitbox)) {
                    data.set(entity, new BehaviourKey(hurtBehaviour, world.getTick()));
                    Health health = data.get(entity, Health.class);
                    if (health != null) {
                        data.set(entity, health.damage(attackbox.damage()));
                    }
                    cancelled = true;
                }
            }
        }

        if (!cancelled) {
            if (attackParams.damageFrom() == elapsedTicks) {
                data.set(entity, new Attackbox(attackParams.damageBox(), attackParams.damage()));
            }
            if (elapsedTicks == attackParams.damageTo() + 1) {
                data.remove(entity, Attackbox.class);
            }
            if (elapsedTicks >= attackParams.attackTicks()) {
                data.set(entity, new BehaviourKey(idleBehaviour, world.getTick()));
            }
        }
    }

    @Override
    public void cleanup(Etherworld world, Map<Integer, Set<PlayerAction>> playerActions, int entity, int elapsedTicks) {
        EntityData data = world.getData();
        data.remove(entity, Attackbox.class);
    }
}
