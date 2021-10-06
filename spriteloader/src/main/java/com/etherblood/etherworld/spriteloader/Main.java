package com.etherblood.etherworld.spriteloader;

import java.io.IOException;

public class Main {
    public static void main(String... args) throws IOException {
        SpriteData data = new SpriteLoader().getFullSprite("assets/src/main/resources/aseprite/sprites/export", "tabby-sheet.json");
    }
}
