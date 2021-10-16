package com.etherblood.etherworld.engine.characters;

import com.etherblood.etherworld.engine.Etherworld;
import com.etherblood.etherworld.engine.PlayerAction;
import java.util.Map;
import java.util.Set;

public interface State {
    void tick(Etherworld world, Map<Integer, Set<PlayerAction>> playerActions, int entity, int elapsedTicks);
}
