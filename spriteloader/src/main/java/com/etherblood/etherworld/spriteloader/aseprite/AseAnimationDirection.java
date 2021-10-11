package com.etherblood.etherworld.spriteloader.aseprite;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum AseAnimationDirection {
    @JsonProperty("forward")
    FORWARD,
    @JsonProperty("reverse")
    REVERSE,
    @JsonProperty("pingpong")
    PING_PONG
}
