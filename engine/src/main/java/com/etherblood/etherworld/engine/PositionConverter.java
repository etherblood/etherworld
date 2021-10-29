package com.etherblood.etherworld.engine;

import com.etherblood.etherworld.engine.chunks.ChunkPosition;
import com.etherblood.etherworld.engine.chunks.ChunkSize;
import com.etherblood.etherworld.engine.chunks.LocalTilePosition;
import com.etherblood.etherworld.engine.chunks.PixelPosition;
import com.etherblood.etherworld.engine.components.Position;
import com.etherblood.etherworld.engine.math.MathUtil;

public class PositionConverter {
    private final int pixelToPosition = 16;
    private final int tileToPixel = 16;
    private final ChunkSize chunkToTile = new ChunkSize(64, 32);

    public int pixelToPosition(int pixel) {
        return pixel * pixelToPosition;
    }

    public int positionToFloorPixel(int position) {
        return Math.floorDiv(position, pixelToPosition);
    }

    public ChunkPosition floorChunk(PixelPosition position) {
        return new ChunkPosition(
                Math.floorDiv(position.x(), chunkToTile.x() * tileToPixel),
                Math.floorDiv(position.y(), chunkToTile.y() * tileToPixel));
    }

    public PixelPosition toPixel(ChunkPosition position) {
        return new PixelPosition(
                position.x() * chunkToTile.x() * tileToPixel,
                position.y() * chunkToTile.y() * tileToPixel);
    }

    public ChunkPosition ceilChunk(PixelPosition position) {
        return new ChunkPosition(
                MathUtil.ceilDiv(position.x(), chunkToTile.x() * tileToPixel),
                MathUtil.ceilDiv(position.y(), chunkToTile.y() * tileToPixel));
    }

    public PixelPosition floorPixel(Position position) {
        return new PixelPosition(
                Math.floorDiv(position.x(), pixelToPosition),
                Math.floorDiv(position.y(), pixelToPosition));
    }

    public int getPixelSize() {
        return pixelToPosition;
    }

    public int getTileSize() {
        return tileToPixel;
    }

    public ChunkSize getChunkSize() {
        return chunkToTile;
    }

    public PixelPosition toPixel(LocalTilePosition localTilePosition) {
        return new PixelPosition(tileToPixel * localTilePosition.x(), tileToPixel * localTilePosition.y());
    }
}
