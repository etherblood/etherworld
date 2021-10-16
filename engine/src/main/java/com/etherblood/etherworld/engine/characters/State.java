package com.etherblood.etherworld.engine.characters;

import com.etherblood.etherworld.engine.Etherworld;
import com.etherblood.etherworld.engine.PlayerAction;
import com.etherblood.etherworld.engine.components.GameCharacter;
import java.util.Map;
import java.util.Set;

public interface State {
    void tick(Etherworld world, Map<Integer, Set<PlayerAction>> playerActions, int entity, GameCharacter gameCharacter, int elapsedTicks);
}
