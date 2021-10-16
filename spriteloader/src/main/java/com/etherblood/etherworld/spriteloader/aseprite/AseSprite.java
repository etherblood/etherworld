package com.etherblood.etherworld.spriteloader.aseprite;

import java.util.List;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public record AseSprite(
        List<AseFrame> frames,
        AseMeta meta) {

    public int frameIndexByMillis(String animation, int millis) {
        List<AseFrame> animationFrames = animationFrames(animation).toList();
        millis = Math.floorMod(millis, animationDurationMillis(animation));
        for (AseFrame frame : animationFrames) {
            millis -= frame.duration();
            if (millis < 0) {
                return frames.indexOf(frame);
            }
        }
        throw new AssertionError(animation + ": " + millis + "ms");
    }

    public int animationDurationMillis(String animation) {
        return animationFrames(animation).mapToInt(AseFrame::duration).sum();
    }

    public Stream<AseFrame> animationFrames(String animation) {
        return meta.frameTags().stream()
                .filter(t -> t.name().equals(animation))
                .flatMapToInt(t -> IntStream.rangeClosed(t.from(), t.to()))
                .mapToObj(frames::get);
    }
}
