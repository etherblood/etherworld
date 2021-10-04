package com.etherblood.etherworld.engine.chunks;

import java.util.BitSet;

public class Chunk {

    private final ChunkSize size;
    private final BitSet obstacleSet;

    public Chunk(ChunkSize size) {
        this.size = size;
        obstacleSet = new BitSet(size.x() * size.y());
    }

    public void setObstacle(LocalTilePosition tilePosition, boolean value) {
        obstacleSet.set(toIndex(tilePosition), value);
    }

    public boolean isObstacle(LocalTilePosition tilePosition) {
        return obstacleSet.get(toIndex(tilePosition));
    }

    private int toIndex(LocalTilePosition point) {
        return point.x() + size.x() * point.y();
    }
}
