package com.etherblood.etherworld.engine.systems;

import com.etherblood.etherworld.data.EntityData;
import com.etherblood.etherworld.engine.Etherworld;
import com.etherblood.etherworld.engine.PlayerAction;
import com.etherblood.etherworld.engine.components.Animation;
import com.etherblood.etherworld.engine.components.CharacterId;
import com.etherblood.etherworld.engine.components.Direction;
import com.etherblood.etherworld.engine.components.Position;
import com.etherblood.etherworld.engine.sprites.GameSprite;
import com.etherblood.etherworld.engine.sprites.GameSpriteAnimation;
import com.etherblood.etherworld.engine.sprites.GameSpriteFrame;
import com.etherblood.etherworld.engine.sprites.GameSpriteHitbox;
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
            for (GameSpriteHitbox attack : frame.attacks()) {
                GameSpriteHitbox attackHitbox;
                if (data.get(entity, Direction.class) == Direction.RIGHT) {
                    attackHitbox = attack.translate(position.x(), position.y());
                } else {
                    attackHitbox = new GameSpriteHitbox(-attack.x() - attack.width(), attack.y(), attack.width(), attack.height()).translate(position.x(), position.y());
                }
                for (int other : data.list(CharacterId.class)) {
                    if (entity == other) {
                        continue;
                    }
                    GameSpriteHitbox otherHitbox = sprites.apply(data.get(other, CharacterId.class).id()).hitbox();
                    Position otherPosition = data.get(other, Position.class);
                    GameSpriteHitbox translated = otherHitbox.translate(otherPosition.x(), otherPosition.y());
                    if (attackHitbox.intersects(translated)) {
                        Animation otherAnimation = data.get(other, Animation.class);
                        if (!"Hit".equals(otherAnimation.animationId())) {
                            data.set(other, new Animation("Hit", 0));
                        }
                    }
                }
            }
        }

    }
}
