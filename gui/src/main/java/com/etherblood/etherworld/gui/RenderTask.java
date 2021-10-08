package com.etherblood.etherworld.gui;

import java.awt.Color;
import java.util.List;
import java.util.Objects;

public record RenderTask(
        Color background,
        RenderRectangle camera,
        List<RenderChunk> chunks,
        List<RenderSprite> sprites,
        List<DebugRectangle> rectangles,
        List<String> lines
) {
    public RenderTask {
        Objects.requireNonNull(background);
        Objects.requireNonNull(camera);
        Objects.requireNonNull(chunks);
        Objects.requireNonNull(sprites);
        Objects.requireNonNull(rectangles);
        Objects.requireNonNull(lines);
    }
}
