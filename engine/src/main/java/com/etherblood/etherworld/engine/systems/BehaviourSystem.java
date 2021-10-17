package com.etherblood.etherworld.engine.systems;

import com.etherblood.etherworld.data.EntityData;
import com.etherblood.etherworld.engine.Etherworld;
import com.etherblood.etherworld.engine.PlayerAction;
import com.etherblood.etherworld.engine.characters.Behaviour;
import com.etherblood.etherworld.engine.components.BehaviourKey;
import java.util.Map;
import java.util.Set;

public class BehaviourSystem implements GameSystem {

    private final Map<String, Behaviour> behaviours;

    public BehaviourSystem(Map<String, Behaviour> behaviours) {
        this.behaviours = behaviours;
    }

    @Override
    public void tick(Etherworld world, Map<Integer, Set<PlayerAction>> playerActions) {
        EntityData data = world.getData();
        for (int entity : data.list(BehaviourKey.class)) {
            BehaviourKey state = data.get(entity, BehaviourKey.class);
            int elapsedTicks = (int) (world.getTick() - state.startTick());
            Behaviour behaviour = behaviours.get(state.value());
            if (behaviour == null) {
                throw new AssertionError("Behaviour " + state.value() + " not found.");
            }
            if (elapsedTicks == 0) {
                behaviour.setup(world, playerActions, entity, elapsedTicks);
            }
            behaviour.tick(world, playerActions, entity, elapsedTicks);
            BehaviourKey newState = data.get(entity, BehaviourKey.class);
            if (!state.equals(newState)) {
                behaviour.cleanup(world, playerActions, entity, elapsedTicks);
            }
        }
    }
}
