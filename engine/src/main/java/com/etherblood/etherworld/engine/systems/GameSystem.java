package com.etherblood.etherworld.engine.systems;

import com.etherblood.etherworld.engine.Etherworld;
import com.etherblood.etherworld.engine.PlayerAction;
import java.util.Map;
import java.util.Set;

public interface GameSystem {
    void tick(Etherworld world, Map<Integer, Set<PlayerAction>> playerActions);

}
