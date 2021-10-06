package com.etherblood.etherworld.gui;

import java.util.List;
import java.util.Objects;

public record RenderTask(
        RenderRectangle camera,
        List<RenderChunk> chunks,
        List<RenderSprite> sprites,
        List<DebugRectangle> rectangles,
        List<String> lines
) {
    public RenderTask {
        Objects.requireNonNull(camera);
        Objects.requireNonNull(chunks);
        Objects.requireNonNull(sprites);
        Objects.requireNonNull(rectangles);
        Objects.requireNonNull(lines);
    }
}
