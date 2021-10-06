package com.etherblood.etherworld.engine;

import com.etherblood.etherworld.data.EntityDatabase;
import com.etherblood.etherworld.engine.chunks.ChunkManager;
import com.etherblood.etherworld.engine.sprites.GameSprite;
import java.util.HashMap;

public class Main {
    public static void main(String... args) {
        HashMap<String, GameSprite> sprites = new HashMap<>();
        Etherworld etherworld = new Etherworld(new EntityDatabase(), sprites::get, new PositionConverter(), new ChunkManager(x -> null));
        GameLoop loop = new GameLoop(60, () -> {
            etherworld.tick(new HashMap<>());
            System.out.println(System.currentTimeMillis());
        });
        loop.run();
    }
}
