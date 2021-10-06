package com.etherblood.etherworld.spriteloader;

import com.etherblood.etherworld.spriteloader.aseprite.AseSprite;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import javax.imageio.ImageIO;

public class SpriteLoader {

    public AseSprite getSpriteInfo(String path, String name) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        File file = Paths.get(path, name).toFile();
        return mapper.readValue(file, AseSprite.class);
    }

    public SpriteData getFullSprite(String path, String name) throws IOException {
        AseSprite data = getSpriteInfo(path, name);
        File file = Paths.get(path, data.meta().image()).toFile();
        BufferedImage image = ImageIO.read(file);
        return new SpriteData(data, image);
    }
}
