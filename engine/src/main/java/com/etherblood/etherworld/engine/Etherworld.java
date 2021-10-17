package com.etherblood.etherworld.engine;

import com.etherblood.etherworld.data.EntityData;
import com.etherblood.etherworld.engine.characters.Behaviour;
import com.etherblood.etherworld.engine.chunks.ChunkManager;
import com.etherblood.etherworld.engine.systems.BehaviourSystem;
import com.etherblood.etherworld.engine.systems.GameSystem;
import com.etherblood.etherworld.engine.systems.GolemSystem;
import com.etherblood.etherworld.engine.systems.MoveSystem;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Etherworld {

    private long tick;
    private final EntityData data;
    private final ChunkManager chunks;
    private final List<GameSystem> systems;

    public Etherworld(EntityData data, ChunkManager chunks, Map<String, Behaviour> behaviours) {
        this.data = data;
        this.chunks = chunks;
        systems = List.of(
                new BehaviourSystem(behaviours),
                new GolemSystem(),
                new MoveSystem()
        );
        tick = 0;
    }

    public void tick(Map<Integer, Set<PlayerAction>> playerActions) {
        for (GameSystem system : systems) {
            system.tick(this, playerActions);
        }
        tick++;
    }

    public long getTick() {
        return tick;
    }

    public EntityData getData() {
        return data;
    }

    public ChunkManager getChunks() {
        return chunks;
    }
}
