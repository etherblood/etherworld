package com.etherblood.etherworld.gui;

import java.awt.image.BufferedImage;
import java.util.Objects;

public record RenderSprite(
        RenderRectangle source,
        RenderRectangle destination,
        BufferedImage sheet
) {
    public RenderSprite {
        Objects.requireNonNull(source);
        Objects.requireNonNull(destination);
        Objects.requireNonNull(sheet);
    }
}
