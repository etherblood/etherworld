package com.etherblood.etherworld.engine;

import com.etherblood.etherworld.data.EntityData;
import com.etherblood.etherworld.engine.chunks.ChunkManager;
import com.etherblood.etherworld.engine.sprites.GameSprite;
import com.etherblood.etherworld.engine.systems.AnimationSystem;
import com.etherblood.etherworld.engine.systems.GameSystem;
import com.etherblood.etherworld.engine.systems.HitSystem;
import com.etherblood.etherworld.engine.systems.MoveSystem;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

public class Etherworld {

    private final EntityData data;
    private final Function<String, GameSprite> sprites;
    private final PositionConverter converter;
    private final ChunkManager chunks;
    private final List<GameSystem> systems;

    public Etherworld(EntityData data, Function<String, GameSprite> sprites, PositionConverter converter, ChunkManager chunks) {
        this.data = data;
        this.sprites = sprites;
        this.converter = converter;
        this.chunks = chunks;
        systems = List.of(
                new HitSystem(),
                new MoveSystem(),
                new AnimationSystem()
        );
    }

    public void tick(Map<Integer, Set<PlayerAction>> playerActions) {
        for (GameSystem system : systems) {
            system.tick(this, playerActions);
        }
    }

    public EntityData getData() {
        return data;
    }

    public Function<String, GameSprite> getSprites() {
        return sprites;
    }

    public PositionConverter getConverter() {
        return converter;
    }

    public ChunkManager getChunks() {
        return chunks;
    }
}
