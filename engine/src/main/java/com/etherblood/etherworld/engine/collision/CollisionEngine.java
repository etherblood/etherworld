package com.etherblood.etherworld.engine.collision;

import com.etherblood.etherworld.engine.RectangleHitbox;
import com.etherblood.etherworld.engine.components.Speed;
import com.etherblood.etherworld.engine.math.Fraction;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

public class CollisionEngine {

    public static final int COLLISION_LIMIT = 10;

    public Map<Integer, List<Collision>> tick(List<Body> bodies,
                                              Function<RectangleHitbox, List<Body>> findKinematicCandidates,
                                              Function<RectangleHitbox, List<Body>> findStaticCandidates) {
        Map<Integer, List<Collision>> result = new HashMap<>();
        while (true) {
            // TODO: broad phase, split bodies into multiple smaller groups which are isolated from each other
            List<Collision> intersections = new ArrayList<>();
            for (Body body : bodies) {
                // TODO: reuse lists from previous iteration when oldBounds.contains(newBounds)?
                RectangleHitbox bounds = body.moveBounds();
                List<Body> kinematics = findKinematicCandidates.apply(bounds);
                List<Body> statics = findStaticCandidates.apply(bounds);

                List<Body> obstacles = new ArrayList<>();
                obstacles.addAll(kinematics);
                obstacles.addAll(statics);

                for (Body obstacle : obstacles) {
                    if (Objects.equals(body.id, obstacle.id)) {
                        continue;
                    }
                    IntersectionInfo info = body.intersect(obstacle);
                    if (info.isIntersecting()
                            && info.time().compareTo(body.simulatedTime) >= 0
                            && info.time().compareTo(obstacle.simulatedTime) >= 0) {

                        // make sure to handle kinematic intersections only once
                        // this might cause objects to clip through kinematics, but they will never clip statics
                        if (kinematics.contains(obstacle)
                                && result.getOrDefault(body.id, Collections.emptyList()).stream()
                                .filter(collision -> collision.b() == obstacle)
                                .count() >= COLLISION_LIMIT) {
                            continue;
                        }

                        intersections.add(new Collision(body, obstacle, info.time(), info.normal()));
                    }
                }
            }
            if (intersections.isEmpty()) {
                break;
            }
            Fraction timeOfIntersection = intersections.stream().min(Comparator.comparing(Collision::timeOfIntersection)).get().timeOfIntersection();
            List<Collision> collisions = intersections.stream()
                    .filter(collision -> collision.timeOfIntersection().compareTo(timeOfIntersection) == 0)
                    .sorted(Comparator.comparingInt(collision -> collision.normal().priority()))

                    //TODO: better workaround for multiple collisions with different axes?
                    .limit(1)
                    .toList();
            for (Collision info : collisions) {
                Body body = info.a();
                Fraction deltaTime = info.timeOfIntersection().subtract(body.simulatedTime);
                body.simulatedPositionX = body.simulatedPositionX.add(deltaTime.multiply(Fraction.ofInt(body.speed.x()))).reduce();
                body.simulatedPositionY = body.simulatedPositionY.add(deltaTime.multiply(Fraction.ofInt(body.speed.y()))).reduce();
                body.simulatedTime = info.timeOfIntersection().reduce();
                Axis axis = info.normal().toAxis();
                Speed nextSpeed = axis.set(body.speed, axis.get(info.b().speed));
                body.speed = nextSpeed;

                // TODO: result should also contain info about positions & speeds at timeOfIntersection?
                result.computeIfAbsent(body.id, x -> new ArrayList<>()).add(info);
            }
        }
        for (Body body : bodies) {
            Fraction deltaTime = Fraction.ofInt(1).subtract(body.simulatedTime);
            body.simulatedPositionX = body.simulatedPositionX.add(deltaTime.multiply(Fraction.ofInt(body.speed.x())));
            body.simulatedPositionY = body.simulatedPositionY.add(deltaTime.multiply(Fraction.ofInt(body.speed.y())));
            body.simulatedTime = Fraction.ofInt(1);
        }
        return result;
    }

}
