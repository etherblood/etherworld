package com.etherblood.etherworld.gui;

import java.awt.Color;
import java.util.Objects;

public record DebugRectangle(
        RenderRectangle destination,
        Color color,
        boolean fill) {
    public DebugRectangle {
        Objects.requireNonNull(destination);
        Objects.requireNonNull(color);
    }
}
