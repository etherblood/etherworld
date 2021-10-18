package com.etherblood.etherworld.engine.golem;

import com.etherblood.etherworld.data.EntityData;
import com.etherblood.etherworld.engine.Etherworld;
import com.etherblood.etherworld.engine.PlayerAction;
import com.etherblood.etherworld.engine.RectangleHitbox;
import com.etherblood.etherworld.engine.components.Attackbox;
import com.etherblood.etherworld.engine.components.BehaviourKey;
import com.etherblood.etherworld.engine.components.FacingDirection;
import com.etherblood.etherworld.engine.components.Health;
import com.etherblood.etherworld.engine.components.Hurtbox;
import com.etherblood.etherworld.engine.components.Movebox;
import com.etherblood.etherworld.engine.components.OnGround;
import com.etherblood.etherworld.engine.components.Position;
import com.etherblood.etherworld.engine.components.Speed;
import com.etherblood.etherworld.engine.golem.components.ChaseTarget;
import com.etherblood.etherworld.engine.golem.components.GolemHand;
import com.etherblood.etherworld.engine.golem.components.GolemHandStateKey;
import com.etherblood.etherworld.engine.systems.GameSystem;
import java.util.Map;
import java.util.Set;

public class GolemHandSystem implements GameSystem {

    private final String headHurtBehaviour;

    public GolemHandSystem(String headHurtBehaviour) {
        this.headHurtBehaviour = headHurtBehaviour;
    }

    @Override
    public void tick(Etherworld world, Map<Integer, Set<PlayerAction>> playerActions) {
        RectangleHitbox scanBase = new RectangleHitbox(-16 * 16 * 12, 0, 16 * 16 * 24, 16 * 16 * 16);
        int chaseSpeed = 4 * 16;
        int jumpStrength = 4 * 16;
        int gravityPerTick = 16;
        int recoverTicks = 40;
        int riseSpeed = 4 * 16;
        Position handOffset = new Position(240 * 16, 96 * 16);
        int resetSpeed = 6 * 16;
        int handDamage = 2;

        EntityData data = world.getData();
        for (int entity : data.list(GolemHand.class)) {
            Position position = data.get(entity, Position.class);
            GolemHandStateKey stateKey = data.get(entity, GolemHandStateKey.class);
            RectangleHitbox scan = scanBase.translate(position.x(), position.y());
            int head = data.get(entity, GolemHand.class).head();
            Position headPosition = data.get(head, Position.class);
            if (data.get(head, Health.class).value() <= 0) {
                if (stateKey.value() != GolemHandState.DEAD) {
                    data.set(entity, new GolemHandStateKey(GolemHandState.DEAD, world.getTick()));
                    data.remove(entity, Attackbox.class);
                    data.set(head, new Position(headPosition.x(), headPosition.y() + 16 * 64));// hacky workaround, this belongs into head code
                }
                Speed speed = data.get(entity, Speed.class);
                data.set(entity, new Speed(0, speed.y() + gravityPerTick));
            } else if (stateKey.value() != GolemHandState.RESET && data.get(head, BehaviourKey.class).value().equals(headHurtBehaviour)) {
                data.set(entity, new GolemHandStateKey(GolemHandState.RESET, world.getTick()));
                data.remove(entity, ChaseTarget.class);
                data.remove(entity, Attackbox.class);
                data.set(entity, new Speed(0, 0));
            } else {
                switch (stateKey.value()) {
                    case RESET:
                        int x = handOffset.x();
                        if (data.get(entity, FacingDirection.class) == FacingDirection.LEFT) {
                            x = -x;
                        }
                        Position resetTarget = headPosition.translate(x, handOffset.y());
                        if (position.equals(resetTarget)) {
                            data.set(entity, new GolemHandStateKey(GolemHandState.SCAN, world.getTick()));
                            data.set(entity, new Speed(0, 0));
                        } else {
                            int dx = resetTarget.x() - position.x();
                            int dy = resetTarget.y() - position.y();
                            int distance = (int) Math.sqrt(dx * dx + dy * dy);
                            Speed speed;
                            if (distance > resetSpeed) {
                                speed = new Speed(dx * resetSpeed / distance, dy * resetSpeed / distance);
                            } else {
                                speed = new Speed(dx, dy);
                            }
                            data.set(entity, speed);
                        }
                        break;
                    case SCAN:
                        for (int other : data.list(Hurtbox.class)) {
                            Position otherPosition = data.get(other, Position.class);
                            RectangleHitbox target = data.get(other, Hurtbox.class).hitbox().translate(otherPosition.x(), otherPosition.y());
                            if (scan.intersects(target)) {
                                data.set(entity, new GolemHandStateKey(GolemHandState.CHASE, world.getTick()));
                                data.set(entity, new ChaseTarget(other));
                                break;
                            }
                        }
                        break;
                    case CHASE:
                        int other = data.get(entity, ChaseTarget.class).target();
                        Position otherPosition = data.get(other, Position.class);
                        RectangleHitbox target = data.get(other, Hurtbox.class).hitbox().translate(otherPosition.x(), otherPosition.y());
                        if (!scan.intersects(target)) {
                            data.set(entity, new GolemHandStateKey(GolemHandState.SCAN, world.getTick()));
                            data.set(entity, new Speed(0, 0));
                            data.remove(entity, ChaseTarget.class);
                            break;
                        }
                        RectangleHitbox hitbox = data.get(entity, Movebox.class).hitbox().translate(position.x(), position.y());
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
                            data.set(entity, new GolemHandStateKey(GolemHandState.SMASH, world.getTick()));
                            data.set(entity, new Speed(0, -jumpStrength));
                            data.set(entity, new Attackbox(data.get(entity, Movebox.class).hitbox(), handDamage));
                        }
                        break;
                    case SMASH:
                        if (data.has(entity, OnGround.class)) {
                            data.set(entity, new GolemHandStateKey(GolemHandState.RECOVER, world.getTick()));
                            data.remove(entity, Attackbox.class);
                        } else {
                            Speed speed = data.get(entity, Speed.class);
                            data.set(entity, new Speed(0, speed.y() + gravityPerTick));
                        }
                        break;
                    case RECOVER:
                        if (world.getTick() >= stateKey.startTick() + recoverTicks) {
                            data.set(entity, new GolemHandStateKey(GolemHandState.RISE, world.getTick()));
                        }
                        break;
                    case RISE:
                        if (position.y() <= headPosition.y() + handOffset.y()) {
                            data.set(entity, new GolemHandStateKey(GolemHandState.SCAN, world.getTick()));
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
}
