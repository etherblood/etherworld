package com.etherblood.etherworld.engine.chunks;

import com.etherblood.etherworld.engine.MathUtil;
import com.etherblood.etherworld.engine.components.Position;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

public class ChunkManager {

    private final int tileSize;
    private final ChunkSize chunkSize;
    private final Map<ChunkPosition, Chunk> chunks = new ConcurrentHashMap<>();
    private final Function<ChunkPosition, Chunk> loader;

    public ChunkManager(int tileSize, ChunkSize chunkSize, Function<ChunkPosition, Chunk> loader) {
        this.tileSize = tileSize;
        this.chunkSize = chunkSize;
        this.loader = loader;
    }

    public int getTileSize() {
        return tileSize;
    }

    public ChunkSize getChunkSize() {
        return chunkSize;
    }

    public Chunk get(ChunkPosition point) {
        return chunks.computeIfAbsent(point, loader);
    }

    public List<Chunk> getAllChunks() {
        return chunks.values().stream().filter(Objects::nonNull).toList();
    }

    public ChunkPosition toChunkPosition(Position position) {
        return toChunkPosition(toFloorTilePosition(position));
    }

    public TilePosition toFloorTilePosition(Position position) {
        return new TilePosition(
                Math.floorDiv(position.x(), tileSize),
                Math.floorDiv(position.y(), tileSize));
    }

    public TilePosition toCeilTilePosition(Position position) {
        return new TilePosition(
                MathUtil.ceilDiv(position.x(), tileSize),
                MathUtil.ceilDiv(position.y(), tileSize));
    }

    public ChunkPosition toChunkPosition(TilePosition position) {
        return new ChunkPosition(
                Math.floorDiv(position.x(), chunkSize.x()),
                Math.floorDiv(position.y(), chunkSize.y()));
    }

    public LocalTilePosition toLocalChunkPosition(TilePosition position) {
        return new LocalTilePosition(
                Math.floorMod(position.x(), chunkSize.x()),
                Math.floorMod(position.y(), chunkSize.y()));
    }

    public void clear() {
        chunks.clear();
    }

}
