package com.etherblood.etherworld.spriteloader;

import java.io.IOException;

class Main {
    public static void main(String... args) throws IOException {
        SpriteData data = new SpriteLoader().getFullSprite("assets/sprites", "Tabby.json");
    }
}
