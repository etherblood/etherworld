package com.etherblood.etherworld.spriteloader.aseprite;

import java.util.List;

public record AseSlice(
        String name,
        String color,
        List<AseSliceKey> keys,
        String data) {
}
