package com.etherblood.etherworld.engine.characters;

import com.etherblood.etherworld.data.EntityData;
import com.etherblood.etherworld.engine.EntityState;
import com.etherblood.etherworld.engine.Etherworld;
import com.etherblood.etherworld.engine.PlayerAction;
import com.etherblood.etherworld.engine.components.CharacterState;
import com.etherblood.etherworld.engine.components.GameCharacter;
import com.etherblood.etherworld.engine.components.Speed;
import java.util.Map;
import java.util.Set;

public class HurtState implements State {

    @Override
    public void tick(Etherworld world, Map<Integer, Set<PlayerAction>> playerActions, int entity, GameCharacter gameCharacter, int elapsedTicks) {
        EntityData data = world.getData();
        PhysicParams physicParams = gameCharacter.physicParams();
        HurtParams hurtParams = gameCharacter.hurtParams();
        Speed speed = data.get(entity, Speed.class);
        if (speed != null) {
            data.set(entity, new Speed(0, speed.y() + physicParams.gravityPerTick()));
        } else {
            data.set(entity, new Speed(0, physicParams.gravityPerTick()));
        }
        if (elapsedTicks >= hurtParams.hurtTicks()) {
            data.set(entity, new CharacterState(EntityState.IDLE, world.getTick()));
        }
    }
}
