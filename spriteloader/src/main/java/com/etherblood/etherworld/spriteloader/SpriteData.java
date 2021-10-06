package com.etherblood.etherworld.spriteloader;

import com.etherblood.etherworld.spriteloader.aseprite.AseSprite;
import java.awt.image.BufferedImage;

public class SpriteData {
    public AseSprite info;
    public BufferedImage image;

    public SpriteData(AseSprite info, BufferedImage image) {
        this.info = info;
        this.image = image;
    }
}
