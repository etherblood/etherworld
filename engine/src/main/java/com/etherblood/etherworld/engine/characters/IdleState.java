package com.etherblood.etherworld.engine.characters;

import com.etherblood.etherworld.data.EntityData;
import com.etherblood.etherworld.engine.EntityState;
import com.etherblood.etherworld.engine.Etherworld;
import com.etherblood.etherworld.engine.PlayerAction;
import com.etherblood.etherworld.engine.components.CharacterState;
import com.etherblood.etherworld.engine.components.FacingDirection;
import com.etherblood.etherworld.engine.components.Health;
import com.etherblood.etherworld.engine.components.OnGround;
import com.etherblood.etherworld.engine.components.OwnerId;
import com.etherblood.etherworld.engine.components.Speed;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

public class IdleState implements State {

    private final PhysicParams physicParams;

    public IdleState(PhysicParams physicParams) {
        this.physicParams = physicParams;
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
            if (playerActions.get(owner.id()).contains(PlayerAction.ATTACK)) {
                data.set(entity, new CharacterState(EntityState.ATTACK, world.getTick()));
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
        Health health = data.get(entity, Health.class);
        if (health != null && health.value() <= 0) {
            data.set(entity, new CharacterState(EntityState.DEAD, world.getTick()));
        }
    }
}
