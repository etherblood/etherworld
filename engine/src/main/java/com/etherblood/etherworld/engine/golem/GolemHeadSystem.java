package com.etherblood.etherworld.engine.golem;

import com.etherblood.etherworld.data.EntityData;
import com.etherblood.etherworld.engine.EntityUtil;
import com.etherblood.etherworld.engine.Etherworld;
import com.etherblood.etherworld.engine.PlayerAction;
import com.etherblood.etherworld.engine.characters.components.HurtParams;
import com.etherblood.etherworld.engine.components.Attackbox;
import com.etherblood.etherworld.engine.components.Health;
import com.etherblood.etherworld.engine.components.Hurtbox;
import com.etherblood.etherworld.engine.components.Movebox;
import com.etherblood.etherworld.engine.components.Obstaclebox;
import com.etherblood.etherworld.engine.components.Speed;
import com.etherblood.etherworld.engine.golem.components.GolemHeadStateKey;
import com.etherblood.etherworld.engine.systems.GameSystem;
import java.util.Map;
import java.util.Set;

public class GolemHeadSystem implements GameSystem {

    @Override
    public void tick(Etherworld world, Map<Integer, Set<PlayerAction>> playerActions) {
        int gravityPerTick = 16;
        EntityData data = world.getData();
        for (int entity : data.list(GolemHeadStateKey.class)) {
            GolemHeadStateKey stateKey = data.get(entity, GolemHeadStateKey.class);
            switch (stateKey.value()) {
                case IDLE -> {
                    Map<Integer, Attackbox> attacks = EntityUtil.findAttacks(data, entity);
                    int damage = attacks.values().stream().mapToInt(Attackbox::damage).sum();
                    if (damage > 0) {
                        data.set(entity, new GolemHeadStateKey(GolemHeadState.HURT, world.getTick()));
                        Health health = data.get(entity, Health.class);
                        if (health != null) {
                            data.set(entity, health.damage(damage));
                        }
                    }
                    if (data.get(entity, Health.class).value() <= 0) {
                        data.set(entity, new Movebox(data.get(entity, Obstaclebox.class).hitbox()));
                        data.set(entity, new GolemHeadStateKey(GolemHeadState.DEAD, world.getTick()));
                    }
                }
                case HURT -> {
                    HurtParams hurtParams = data.get(entity, HurtParams.class);
                    if (world.getTick() >= stateKey.startTick() + hurtParams.hurtTicks()) {
                        data.set(entity, new GolemHeadStateKey(GolemHeadState.IDLE, world.getTick()));
                    }
                }
                case DEAD -> {
                    Speed speed = data.get(entity, Speed.class);
                    data.set(entity, new Speed(speed.x(), speed.y() + gravityPerTick));
                    data.remove(entity, Hurtbox.class);
                }
                default -> throw new AssertionError(stateKey.value());
            }
        }
    }
}
