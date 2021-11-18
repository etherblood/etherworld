package com.etherblood.etherworld.gui;

import java.awt.Color;
import java.util.List;
import java.util.Objects;

public record RenderTask(
        int scale,
        long tick,
        Color background,
        RenderRectangle camera,
        List<RenderChunk> chunks,
        List<RenderSprite> sprites,
        List<DebugRectangle> rectangles,
        List<String> lines
) {
    public RenderTask {
        if (scale <= 0) {
            throw new IllegalArgumentException("Scale must be positive.");
        }
        Objects.requireNonNull(background);
        Objects.requireNonNull(camera);
        Objects.requireNonNull(chunks);
        Objects.requireNonNull(sprites);
        Objects.requireNonNull(rectangles);
        Objects.requireNonNull(lines);
    }
}
