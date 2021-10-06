package com.etherblood.etherworld.gui;

import java.awt.image.BufferedImage;
import java.util.Objects;

public record RenderChunk(
        RenderRectangle background,
        RenderRectangle hitbox,
        RenderRectangle foreground,
        RenderRectangle destination,
        BufferedImage sheet
) {
    public RenderChunk {
        Objects.requireNonNull(background);
        Objects.requireNonNull(hitbox);
        Objects.requireNonNull(foreground);
        Objects.requireNonNull(destination);
        Objects.requireNonNull(sheet);
    }
}
