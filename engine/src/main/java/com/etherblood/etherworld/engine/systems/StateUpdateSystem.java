package com.etherblood.etherworld.engine.systems;

import com.etherblood.etherworld.data.EntityData;
import com.etherblood.etherworld.engine.EntityState;
import com.etherblood.etherworld.engine.Etherworld;
import com.etherblood.etherworld.engine.PlayerAction;
import com.etherblood.etherworld.engine.characters.AttackState;
import com.etherblood.etherworld.engine.characters.DeadState;
import com.etherblood.etherworld.engine.characters.HurtState;
import com.etherblood.etherworld.engine.characters.IdleState;
import com.etherblood.etherworld.engine.components.CharacterState;
import com.etherblood.etherworld.engine.components.GameCharacter;
import java.util.Map;
import java.util.Set;

public class StateUpdateSystem implements GameSystem {

    @Override
    public void tick(Etherworld world, Map<Integer, Set<PlayerAction>> playerActions) {
        EntityData data = world.getData();
        for (int entity : data.list(CharacterState.class)) {
            CharacterState state = data.get(entity, CharacterState.class);
            int elapsedTicks = (int) (world.getTick() - state.startTick());
            tick(world, playerActions, entity, state.value(), elapsedTicks);
        }
    }

    private void tick(Etherworld world, Map<Integer, Set<PlayerAction>> playerActions, int entity, EntityState state, int elapsedTicks) {
        EntityData data = world.getData();
        GameCharacter gameCharacter = data.get(entity, GameCharacter.class);
        switch (state) {
            case IDLE -> new IdleState().tick(world, playerActions, entity, gameCharacter, elapsedTicks);
            case ATTACK -> new AttackState().tick(world, playerActions, entity, gameCharacter, elapsedTicks);
            case HURT -> new HurtState().tick(world, playerActions, entity, gameCharacter, elapsedTicks);
            case DEAD -> new DeadState().tick(world, playerActions, entity, gameCharacter, elapsedTicks);
            default -> throw new AssertionError(state);
        }
    }
}
