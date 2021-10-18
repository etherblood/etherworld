package com.etherblood.etherworld.engine;

import com.etherblood.etherworld.data.EntityData;
import com.etherblood.etherworld.engine.components.Attackbox;
import com.etherblood.etherworld.engine.components.FacingDirection;
import com.etherblood.etherworld.engine.components.Hurtbox;
import com.etherblood.etherworld.engine.components.Position;
import java.util.LinkedHashMap;
import java.util.Map;

public class EntityUtil {

    public static Map<Integer, Attackbox> findAttacks(EntityData data, int entity) {
        Map<Integer, Attackbox> result = new LinkedHashMap<>();
        Hurtbox hurtbox = data.get(entity, Hurtbox.class);
        if (hurtbox != null) {
            Position position = data.get(entity, Position.class);
            RectangleHitbox hurtHitbox = hurtbox.hitbox().translate(position.x(), position.y());
            if (data.get(entity, FacingDirection.class) == FacingDirection.LEFT) {
                hurtHitbox = hurtHitbox.mirrorX(position.x());
            }
            for (int other : data.list(Attackbox.class)) {
                if (entity == other) {
                    continue;
                }
                Position otherPosition = data.get(other, Position.class);
                Attackbox attackbox = data.get(other, Attackbox.class);
                RectangleHitbox attackHitbox = attackbox.hitbox().translate(otherPosition.x(), otherPosition.y());
                if (data.get(other, FacingDirection.class) == FacingDirection.LEFT) {
                    attackHitbox = attackHitbox.mirrorX(otherPosition.x());
                }
                if (attackHitbox.intersects(hurtHitbox)) {
                    result.put(other, attackbox);
                }
            }
        }
        return result;
    }
}
