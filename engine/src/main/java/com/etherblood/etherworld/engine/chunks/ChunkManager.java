package com.etherblood.etherworld.engine.chunks;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

public class ChunkManager {

    private final Map<ChunkPosition, Chunk> chunks = new HashMap<>();
    private final Function<ChunkPosition, Chunk> loader;

    public ChunkManager(Function<ChunkPosition, Chunk> loader) {
        this.loader = loader;
    }

    public Chunk get(ChunkPosition point) {
        return chunks.computeIfAbsent(point, loader);
    }

    public List<Chunk> getAllChunks() {
        return chunks.values().stream().filter(Objects::nonNull).toList();
    }
}
