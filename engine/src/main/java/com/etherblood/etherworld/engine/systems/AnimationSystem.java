package com.etherblood.etherworld.engine.systems;

import com.etherblood.etherworld.data.EntityData;
import com.etherblood.etherworld.engine.Etherworld;
import com.etherblood.etherworld.engine.PlayerAction;
import com.etherblood.etherworld.engine.components.Animation;
import com.etherblood.etherworld.engine.components.CharacterId;
import com.etherblood.etherworld.engine.components.FacingDirection;
import com.etherblood.etherworld.engine.components.OnGround;
import com.etherblood.etherworld.engine.components.OwnerId;
import com.etherblood.etherworld.engine.components.Speed;
import com.etherblood.etherworld.engine.sprites.GameSprite;
import com.etherblood.etherworld.engine.sprites.GameSpriteAnimation;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

public class AnimationSystem implements GameSystem {

    @Override
    public void tick(Etherworld world, Map<Integer, Set<PlayerAction>> playerActions) {
        EntityData data = world.getData();
        Function<String, GameSprite> sprites = world.getSprites();

        for (int entity : data.list(Animation.class)) {
            String characterId = data.get(entity, CharacterId.class).id();
            Animation animation = data.get(entity, Animation.class);
            GameSprite sprite = sprites.apply(characterId);
            GameSpriteAnimation spriteAnimation = sprite.animations().get(animation.animationId());

            boolean fallBackState = true;
            String nextAnimation = animation.animationId();

            OwnerId owner = data.get(entity, OwnerId.class);
            if (owner != null) {
                if (playerActions.get(owner.id()).contains(PlayerAction.ATTACK)
                        && !"Attack".equals(animation.animationId())
                        && !"Hit".equals(animation.animationId())) {
                    nextAnimation = "Attack";
                    fallBackState = false;
                }
            }

            int ticks = animation.elapsedTicks() + 1;
            switch (animation.animationId()) {
                case "Attack":
                case "Hit":
                    if (ticks < spriteAnimation.totalTicks()) {
                        fallBackState = false;
                    }
                    break;
                default:
                    break;
            }
            if (fallBackState) {
                Speed speed = data.get(entity, Speed.class);
                if (speed == null) {
                    speed = new Speed(0, 0);
                }
                if (speed.x() < 0) {
                    data.set(entity, FacingDirection.LEFT);
                } else if (speed.x() > 0) {
                    data.set(entity, FacingDirection.RIGHT);
                }
                if (data.has(entity, OnGround.class)) {
                    if (speed.x() == 0) {
                        nextAnimation = "Stand";
                    } else {
                        nextAnimation = "Run";
                    }
                } else {
                    if (speed.y() < 0) {
                        nextAnimation = "Up";
                    } else {
                        nextAnimation = "Down";
                    }
                }
            }
            Animation next;
            if (nextAnimation.equals(animation.animationId())) {
                next = new Animation(
                        animation.animationId(),
                        ticks % spriteAnimation.totalTicks());
            } else {
                next = new Animation(
                        nextAnimation,
                        0);
            }
            data.set(entity, next);
        }
    }
}
