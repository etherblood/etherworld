package com.etherblood.etherworld.spriteloader.aseprite;

import java.util.List;

public record AseMeta(
        String app,
        String version,
        String image,
        String format,
        AseSize size,
        int scale,
        List<AseFrameTag> frameTags,
        List<AseLayer> layers,
        List<AseSlice> slices) {
}
