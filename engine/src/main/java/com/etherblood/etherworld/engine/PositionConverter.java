package com.etherblood.etherworld.engine;

import com.etherblood.etherworld.engine.chunks.ChunkPosition;
import com.etherblood.etherworld.engine.chunks.LocalTilePosition;
import com.etherblood.etherworld.engine.chunks.TilePosition;
import com.etherblood.etherworld.engine.components.Position;

public class PositionConverter {
    private final int pixelToPosition = 16;
    private final int tileToPixel = 16;
    private final int chunkToTileX = 64;
    private final int chunkToTileY = 32;

    public ChunkPosition floorChunk(TilePosition position) {
        return new ChunkPosition(
                Math.floorDiv(position.x(), chunkToTileX),
                Math.floorDiv(position.y(), chunkToTileY));
    }

    public LocalTilePosition toLocal(TilePosition position) {
        return new LocalTilePosition(
                Math.floorMod(position.x(), chunkToTileX),
                Math.floorMod(position.y(), chunkToTileY));
    }

    public TilePosition floorTile(Position position) {
        return new TilePosition(
                Math.floorDiv(position.x(), pixelToPosition * tileToPixel),
                Math.floorDiv(position.y(), pixelToPosition * tileToPixel));
    }

    public TilePosition ceilTile(Position position) {
        return new TilePosition(
                MathUtil.ceilDiv(position.x(), pixelToPosition * tileToPixel),
                MathUtil.ceilDiv(position.y(), pixelToPosition * tileToPixel));
    }

    public int tileSize() {
        return tileToPixel * pixelToPosition;
    }
}
