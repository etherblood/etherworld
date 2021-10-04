package com.etherblood.etherworld.engine;

import com.etherblood.etherworld.data.EntityDatabase;
import com.etherblood.etherworld.engine.sprites.Sprite;
import java.util.HashMap;

public class Main {
    public static void main(String... args) {
        HashMap<String, Sprite> sprites = new HashMap<>();
        Etherworld etherworld = new Etherworld(new EntityDatabase(), sprites);
    }
}
