package com.etherblood.etherworld.engine.components;

import com.etherblood.etherworld.engine.characters.HurtParams;
import com.etherblood.etherworld.engine.characters.PhysicParams;
import java.util.Objects;

public record GameCharacter(
        String id,
        PhysicParams physicParams,
        HurtParams hurtParams
) {
    public GameCharacter {
        Objects.requireNonNull(id);
        Objects.requireNonNull(physicParams);
        Objects.requireNonNull(hurtParams);
    }
}
