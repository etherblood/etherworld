package com.etherblood.etherworld.engine.systems;

import com.etherblood.etherworld.data.EntityData;
import com.etherblood.etherworld.engine.Etherworld;
import com.etherblood.etherworld.engine.PlayerAction;
import com.etherblood.etherworld.engine.RectangleHitbox;
import com.etherblood.etherworld.engine.components.Attackbox;
import com.etherblood.etherworld.engine.components.FacingDirection;
import com.etherblood.etherworld.engine.components.Hurtbox;
import com.etherblood.etherworld.engine.components.Movebox;
import com.etherblood.etherworld.engine.components.OnGround;
import com.etherblood.etherworld.engine.components.Position;
import com.etherblood.etherworld.engine.components.Speed;
import com.etherblood.etherworld.engine.components.StateKey;
import com.etherblood.etherworld.engine.components.golem.ChaseTarget;
import com.etherblood.etherworld.engine.components.golem.GolemHand;
import java.util.Map;
import java.util.Set;

public class GolemSystem implements GameSystem {
    @Override
    public void tick(Etherworld world, Map<Integer, Set<PlayerAction>> playerActions) {
        RectangleHitbox scanBase = new RectangleHitbox(-16 * 16 * 12, 0, 16 * 16 * 24, 16 * 16 * 16);
        int chaseSpeed = 4 * 16;
        int jumpStrength = 4 * 16;
        int gravityPerTick = 16;
        int handDamge = 2;
        int recoverTicks = 60;
        int riseSpeed = 4 * 16;

        EntityData data = world.getData();
        for (int entity : data.list(GolemHand.class)) {
            Position position = data.get(entity, Position.class);
            StateKey stateKey = data.get(entity, StateKey.class);
            RectangleHitbox scan = scanBase.translate(position.x(), position.y());
            switch (stateKey.value()) {
                case "GolemHandScan":
                    for (int other : data.list(Hurtbox.class)) {
                        Position otherPosition = data.get(other, Position.class);
                        RectangleHitbox target = data.get(other, Hurtbox.class).hitbox().translate(otherPosition.x(), otherPosition.y());
                        if (scan.intersects(target)) {
                            data.set(entity, new StateKey("GolemHandChase", world.getTick()));
                            data.set(entity, new ChaseTarget(other));
                            break;
                        }
                    }
                    break;
                case "GolemHandChase":
                    int other = data.get(entity, ChaseTarget.class).target();
                    Position otherPosition = data.get(other, Position.class);
                    RectangleHitbox target = data.get(other, Hurtbox.class).hitbox().translate(otherPosition.x(), otherPosition.y());
                    if (!scan.intersects(target)) {
                        data.set(entity, new StateKey("GolemHandScan", world.getTick()));
                        data.set(entity, new Speed(0, 0));
                        data.remove(entity, ChaseTarget.class);
                        break;
                    }
                    RectangleHitbox hitbox = data.get(entity, Movebox.class).hitbox().translate(position.x(), position.y());
                    int head = data.get(entity, GolemHand.class).head();
                    Position headPosition = data.get(head, Position.class);
                    if (target.maxX() < position.x()) {
                        int vx = -chaseSpeed;
                        if (data.get(entity, FacingDirection.class) != FacingDirection.LEFT) {
                            vx = Math.max(vx, headPosition.x() - hitbox.minX());
                        }
                        data.set(entity, new Speed(vx, 0));
                    } else if (target.minX() > position.x()) {
                        int vx = chaseSpeed;
                        if (data.get(entity, FacingDirection.class) == FacingDirection.LEFT) {
                            vx = Math.min(vx, headPosition.x() - hitbox.maxX());
                        }
                        data.set(entity, new Speed(vx, 0));
                    } else {
                        data.set(entity, new StateKey("GolemHandSmash", world.getTick()));
                        data.set(entity, new Speed(0, -jumpStrength));
                        data.set(entity, new Attackbox(data.get(entity, Movebox.class).hitbox(), handDamge));
                    }
                    break;
                case "GolemHandSmash":
                    if (data.has(entity, OnGround.class)) {
                        data.remove(entity, Attackbox.class);
                        data.set(entity, new StateKey("GolemHandRecover", world.getTick()));
                    } else {
                        Speed speed = data.get(entity, Speed.class);
                        data.set(entity, new Speed(0, speed.y() + gravityPerTick));
                    }
                    break;
                case "GolemHandRecover":
                    if (world.getTick() >= stateKey.startTick() + recoverTicks) {
                        data.set(entity, new StateKey("GolemHandRise", world.getTick()));
                    }
                    break;
                case "GolemHandRise":
                    if (position.y() <= 16 * 832) {
                        data.set(entity, new StateKey("GolemHandScan", world.getTick()));
                        data.set(entity, new Speed(0, 0));
                    } else {
                        data.set(entity, new Speed(0, -riseSpeed));
                    }
                    break;
                default:
                    throw new AssertionError(stateKey.value());
            }
        }
    }
}
