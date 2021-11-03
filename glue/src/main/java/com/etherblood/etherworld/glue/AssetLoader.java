package com.etherblood.etherworld.glue;

import com.etherblood.etherworld.engine.chunks.ChunkPosition;
import com.etherblood.etherworld.spriteloader.SpriteData;
import com.etherblood.etherworld.spriteloader.SpriteLoader;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class AssetLoader {
    private final SpriteLoader spriteLoader = new SpriteLoader();
    private final String chunkPath;
    private final String spritePath;
    private final Map<ChunkPosition, SpriteData> chunks = new ConcurrentHashMap<>();
    private final Map<String, SpriteData> sprites = new ConcurrentHashMap<>();

    public AssetLoader(String chunkPath, String spritePath) {
        this.chunkPath = chunkPath;
        this.spritePath = spritePath;
    }

    public SpriteData loadChunk(ChunkPosition position) {
        return chunks.computeIfAbsent(position, p -> {
            try {
                return spriteLoader.getFullSprite(chunkPath, "(" + p.x() + "," + p.y() + ").json");
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    public SpriteData loadSprite(String name) {
        return sprites.computeIfAbsent(name, n -> {
            try {
                return spriteLoader.getFullSprite(spritePath, n + ".json");
            } catch (IOException e) {
                throw new RuntimeException("Error when loading " + spritePath + "/" + n + ".json", e);
            }
        });
    }

    public void clear() {
        chunks.clear();
        sprites.clear();
    }
}
