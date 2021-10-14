package com.etherblood.etherworld.engine.systems;

import com.etherblood.etherworld.data.EntityData;
import com.etherblood.etherworld.engine.Etherworld;
import com.etherblood.etherworld.engine.PlayerAction;
import com.etherblood.etherworld.engine.RectangleHitbox;
import com.etherblood.etherworld.engine.components.Animation;
import com.etherblood.etherworld.engine.components.CharacterId;
import com.etherblood.etherworld.engine.components.FacingDirection;
import com.etherblood.etherworld.engine.components.Health;
import com.etherblood.etherworld.engine.components.Position;
import com.etherblood.etherworld.engine.sprites.GameSprite;
import com.etherblood.etherworld.engine.sprites.GameSpriteAnimation;
import com.etherblood.etherworld.engine.sprites.GameSpriteFrame;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

public class HitSystem implements GameSystem {

    @Override
    public void tick(Etherworld world, Map<Integer, Set<PlayerAction>> playerActions) {
        EntityData data = world.getData();
        Function<String, GameSprite> sprites = world.getSprites();

        for (int entity : data.list(Animation.class)) {
            String characterId = data.get(entity, CharacterId.class).id();
            Animation animation = data.get(entity, Animation.class);
            GameSprite sprite = sprites.apply(characterId);
            GameSpriteAnimation spriteAnimation = sprite.animations().get(animation.animationId());
            GameSpriteFrame frame = spriteAnimation.frameByTick(animation.elapsedTicks());
            Position position = data.get(entity, Position.class);
            for (RectangleHitbox attack : frame.attacks()) {
                RectangleHitbox attackHitbox;
                if (data.get(entity, FacingDirection.class) == FacingDirection.RIGHT) {
                    attackHitbox = attack.translate(position.x(), position.y());
                } else {
                    attackHitbox = new RectangleHitbox(-attack.x() - attack.width(), attack.y(), attack.width(), attack.height()).translate(position.x(), position.y());
                }
                for (int other : data.list(CharacterId.class)) {
                    if (entity == other) {
                        continue;
                    }
                    RectangleHitbox otherHitbox = sprites.apply(data.get(other, CharacterId.class).id()).hitbox();
                    Position otherPosition = data.get(other, Position.class);
                    RectangleHitbox translated = otherHitbox.translate(otherPosition.x(), otherPosition.y());
                    if (attackHitbox.intersects(translated)) {
                        Animation otherAnimation = data.get(other, Animation.class);
                        if (!"Hit".equals(otherAnimation.animationId())) {
                            data.set(other, new Animation("Hit", 0));
                            Health health = data.get(other, Health.class);
                            if (health != null) {
                                data.set(other, health.damage(1));
                            }
                        }
                    }
                }
            }
        }

    }
}
