package com.etherblood.etherworld.glue;

import com.etherblood.etherworld.data.EntityData;
import com.etherblood.etherworld.data.EntityDatabase;
import com.etherblood.etherworld.engine.Etherworld;
import com.etherblood.etherworld.engine.GameLoop;
import com.etherblood.etherworld.engine.PlayerAction;
import com.etherblood.etherworld.engine.PositionConverter;
import com.etherblood.etherworld.engine.RectangleHitbox;
import com.etherblood.etherworld.engine.characters.AttackBehaviour;
import com.etherblood.etherworld.engine.characters.AttackParams;
import com.etherblood.etherworld.engine.characters.Behaviour;
import com.etherblood.etherworld.engine.characters.DeadBehaviour;
import com.etherblood.etherworld.engine.characters.HurtBehaviour;
import com.etherblood.etherworld.engine.characters.HurtParams;
import com.etherblood.etherworld.engine.characters.IdleBehaviour;
import com.etherblood.etherworld.engine.characters.PhysicParams;
import com.etherblood.etherworld.engine.chunks.Chunk;
import com.etherblood.etherworld.engine.chunks.ChunkManager;
import com.etherblood.etherworld.engine.chunks.ChunkPosition;
import com.etherblood.etherworld.engine.chunks.LocalTilePosition;
import com.etherblood.etherworld.engine.chunks.PixelPosition;
import com.etherblood.etherworld.engine.components.Attackbox;
import com.etherblood.etherworld.engine.components.BehaviourKey;
import com.etherblood.etherworld.engine.components.FacingDirection;
import com.etherblood.etherworld.engine.components.GameCharacter;
import com.etherblood.etherworld.engine.components.Health;
import com.etherblood.etherworld.engine.components.Hurtbox;
import com.etherblood.etherworld.engine.components.Movebox;
import com.etherblood.etherworld.engine.components.MovingPlatform;
import com.etherblood.etherworld.engine.components.Obstaclebox;
import com.etherblood.etherworld.engine.components.OwnerId;
import com.etherblood.etherworld.engine.components.Position;
import com.etherblood.etherworld.engine.components.Respawn;
import com.etherblood.etherworld.engine.components.Speed;
import com.etherblood.etherworld.gui.DebugRectangle;
import com.etherblood.etherworld.gui.Gui;
import com.etherblood.etherworld.gui.RenderChunk;
import com.etherblood.etherworld.gui.RenderRectangle;
import com.etherblood.etherworld.gui.RenderSprite;
import com.etherblood.etherworld.gui.RenderTask;
import com.etherblood.etherworld.spriteloader.SpriteData;
import com.etherblood.etherworld.spriteloader.aseprite.AseFrame;
import com.etherblood.etherworld.spriteloader.aseprite.AseSlice;
import com.etherblood.etherworld.spriteloader.aseprite.AseSliceKey;
import java.awt.Color;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

class Main {

    private static final int TICKS_PER_SECOND = 60;
    private static final int MILLIS_PER_SECOND = 1000;
    public static final String BEHAVIOUR_IDLE = "IDLE";
    public static final String BEHAVIOUR_ATTACK = "ATTACK";
    public static final String BEHAVIOUR_HURT = "HURT";
    public static final String BEHAVIOUR_DEAD = "DEAD";

    public static void main(String... args) {
        AssetLoader assetLoader = new AssetLoader(
                "assets/chunks",
                "assets/sprites");

        // only chunks from this set will be loaded
        // TODO: create world/map file with chunkPosition -> chunkFile mapping
        Set<ChunkPosition> worldChunks = Set.of(
                new ChunkPosition(-1, -1),
                new ChunkPosition(0, -1),
                new ChunkPosition(-1, 0),
                new ChunkPosition(0, 0),
                new ChunkPosition(1, 0),
                new ChunkPosition(2, 0),
                new ChunkPosition(2, 1),
                new ChunkPosition(2, 2)
        );
        Map<String, Behaviour> behaviours = new HashMap<>();
        EntityDatabase data = new EntityDatabase();
        PositionConverter converter = new PositionConverter();
        Etherworld world = new Etherworld(
                data,
                new ChunkManager(
                        converter.getTileSize() * converter.getPixelSize(),
                        converter.getChunkSize(),
                        position -> worldChunks.contains(position) ? convert(assetLoader.loadChunk(position), converter) : null),
                behaviours
        );

        int player = data.createEntity();
        int tabby = createCharacter(world, assetLoader, behaviours, converter, "Tabby");
        data.set(tabby, new OwnerId(player));
        data.set(tabby, FacingDirection.RIGHT);
        data.set(tabby, new Position(0, 23 * 16 * 16));
        data.set(tabby, new Respawn(data.get(tabby, Position.class)));
        data.set(tabby, new Health(5, 5));

        int slime = createCharacter(world, assetLoader, behaviours, converter, "Slime");
        data.set(slime, FacingDirection.LEFT);
        data.set(slime, new Position(800 * converter.getPixelSize(), 24 * 16 * 16));
        data.set(slime, new Respawn(data.get(slime, Position.class)));
        data.set(slime, new Health(2, 2));
        data.set(slime, new Attackbox(data.get(slime, Hurtbox.class).hitbox(), 1));

        int dummy = createCharacter(world, assetLoader, behaviours, converter, "Tabby");
        data.set(dummy, FacingDirection.LEFT);
        data.set(dummy, new Position(900 * converter.getPixelSize(), 0));
        data.set(dummy, new Health(10, 10));

        int amara = createCharacter(world, assetLoader, behaviours, converter, "Amara");
        data.set(amara, FacingDirection.LEFT);
        data.set(amara, new Position(1000 * converter.getPixelSize(), 24 * 16 * 16));

        int fallacia = createCharacter(world, assetLoader, behaviours, converter, "Fallacia");
        data.set(fallacia, FacingDirection.LEFT);
        data.set(fallacia, new Position(1100 * converter.getPixelSize(), 24 * 16 * 16));

        {
            String name = "GolemHand";
            SpriteData sprite = assetLoader.loadSprite(name);
            AseSlice hitboxSlice = sprite.info.meta().slices().stream().filter(x -> x.name().equals("Hitbox")).findFirst().get();
            AseSliceKey hitboxKey = hitboxSlice.keys().get(0);
            Position pivot = new Position(
                    converter.pixelToPosition(hitboxKey.pivot().x() + hitboxKey.bounds().x()),
                    converter.pixelToPosition(hitboxKey.pivot().y() + hitboxKey.bounds().y()));
            RectangleHitbox hitbox = new RectangleHitbox(
                    converter.pixelToPosition(hitboxKey.bounds().x()) - pivot.x(),
                    converter.pixelToPosition(hitboxKey.bounds().y()) - pivot.y(),
                    converter.pixelToPosition(hitboxKey.bounds().w()),
                    converter.pixelToPosition(hitboxKey.bounds().h()));

            int entity = data.createEntity();
            data.set(entity, new Position(2750 * converter.getPixelSize(), 768 * converter.getPixelSize()));
            data.set(entity, new Obstaclebox(hitbox));
            data.set(entity, new GameCharacter(
                    name,
                    new PhysicParams(0, 0, 0, 0),
                    new HurtParams(0, 0)));
        }

        {
            String name = "Platform1";
            SpriteData sprite = assetLoader.loadSprite(name);
            AseSlice hitboxSlice = sprite.info.meta().slices().stream().filter(x -> x.name().equals("Obstacle")).findFirst().get();
            AseSliceKey hitboxKey = hitboxSlice.keys().get(0);
            Position pivot = new Position(
                    converter.pixelToPosition(hitboxKey.pivot().x() + hitboxKey.bounds().x()),
                    converter.pixelToPosition(hitboxKey.pivot().y() + hitboxKey.bounds().y()));
            RectangleHitbox hitbox = new RectangleHitbox(
                    converter.pixelToPosition(hitboxKey.bounds().x()) - pivot.x(),
                    converter.pixelToPosition(hitboxKey.bounds().y()) - pivot.y(),
                    converter.pixelToPosition(hitboxKey.bounds().w()),
                    converter.pixelToPosition(hitboxKey.bounds().h()));

            int entity = data.createEntity();
            RectangleHitbox platformPath = new RectangleHitbox(
                    -50 * converter.getPixelSize(),
                    -50 * converter.getPixelSize(),
                    400 * converter.getPixelSize(),
                    400 * converter.getPixelSize());
            data.set(entity, new MovingPlatform(name, platformPath, -64));
            data.set(entity, new Position(platformPath.x(), platformPath.y()));
            data.set(entity, new Obstaclebox(hitbox));
        }

        Gui gui = new Gui();
        gui.start();
        gui.render(createRenderTask(world, assetLoader::loadSprite, position -> worldChunks.contains(position) ? assetLoader.loadChunk(position) : null, tabby, converter));

        Map<Integer, PlayerAction> actionMappings = Map.of(
                KeyEvent.VK_LEFT, PlayerAction.LEFT,
                KeyEvent.VK_UP, PlayerAction.JUMP,
                KeyEvent.VK_RIGHT, PlayerAction.RIGHT,
                KeyEvent.VK_A, PlayerAction.ATTACK
        );

        GameLoop loop = new GameLoop(TICKS_PER_SECOND, () -> {
            world.tick(Map.of(player, gui.getPressedKeys().stream().map(actionMappings::get).collect(Collectors.toSet())));
            gui.render(createRenderTask(world, assetLoader::loadSprite, position -> worldChunks.contains(position) ? assetLoader.loadChunk(position) : null, tabby, converter));
        });
        loop.run();
    }

    private static int createCharacter(Etherworld world, AssetLoader assetLoader, Map<String, Behaviour> behaviours, PositionConverter converter, String name) {
        SpriteData sprite = assetLoader.loadSprite(name);
        EntityData data = world.getData();
        AseSlice hitboxSlice = sprite.info.meta().slices().stream().filter(x -> x.name().equals("Hitbox")).findFirst().get();
        AseSliceKey hitboxKey = hitboxSlice.keys().get(0);
        Position pivot = new Position(
                converter.pixelToPosition(hitboxKey.pivot().x() + hitboxKey.bounds().x()),
                converter.pixelToPosition(hitboxKey.pivot().y() + hitboxKey.bounds().y()));
        RectangleHitbox hitbox = new RectangleHitbox(
                converter.pixelToPosition(hitboxKey.bounds().x()) - pivot.x(),
                converter.pixelToPosition(hitboxKey.bounds().y()) - pivot.y(),
                converter.pixelToPosition(hitboxKey.bounds().w()),
                converter.pixelToPosition(hitboxKey.bounds().h()));


        Optional<AseSliceKey> optionalDamageKey = sprite.info.meta().slices().stream()
                .filter(x -> x.name().equals("Damage"))
                .flatMap(x -> x.keys().stream())
                .findFirst();
        if (optionalDamageKey.isPresent()) {
            AseSliceKey damageKey = optionalDamageKey.get();
            RectangleHitbox damagebox = new RectangleHitbox(
                    converter.pixelToPosition(damageKey.bounds().x()) - pivot.x(),
                    converter.pixelToPosition(damageKey.bounds().y()) - pivot.y(),
                    converter.pixelToPosition(damageKey.bounds().w()),
                    converter.pixelToPosition(damageKey.bounds().h()));

            Integer startMillis = null;
            Integer endMillis = null;
            int millis = 0;
            List<AseFrame> attackFrames = sprite.info.animationFrames("Attack").toList();
            for (AseFrame attackFrame : attackFrames) {
                int frameIndex = sprite.info.frames().indexOf(attackFrame);
                if (damageKey.frame() == frameIndex) {
                    if (startMillis == null) {
                        startMillis = millis;
                    }
                } else {
                    if (startMillis != null && endMillis == null) {
                        endMillis = millis - 1;
                    }
                }
                millis += attackFrame.duration();
            }

            if (startMillis != null && endMillis != null) {
                AttackParams attackParams = new AttackParams(
                        damagebox,
                        startMillis * TICKS_PER_SECOND / MILLIS_PER_SECOND,
                        endMillis * TICKS_PER_SECOND / MILLIS_PER_SECOND,
                        1,
                        sprite.info.animationDurationMillis("Attack") * TICKS_PER_SECOND / MILLIS_PER_SECOND);
                behaviours.put(name + BEHAVIOUR_ATTACK, new AttackBehaviour(name + BEHAVIOUR_IDLE, name + BEHAVIOUR_HURT, attackParams));
            }
        }

        behaviours.put(name + BEHAVIOUR_IDLE, new IdleBehaviour(name + BEHAVIOUR_ATTACK, name + BEHAVIOUR_HURT, name + BEHAVIOUR_DEAD));
        behaviours.put(name + BEHAVIOUR_HURT, new HurtBehaviour(name + BEHAVIOUR_IDLE));
        behaviours.put(name + BEHAVIOUR_DEAD, new DeadBehaviour(name + BEHAVIOUR_IDLE));

        PhysicParams physicParams = new PhysicParams(8 * 16, 20, 16 * 16, 12);
        HurtParams hurtParams = new HurtParams(
                sprite.info.animationDurationMillis("Hit") * TICKS_PER_SECOND / MILLIS_PER_SECOND,
                5 * TICKS_PER_SECOND);
        int entity = data.createEntity();
        data.set(entity, new Hurtbox(hitbox));
        data.set(entity, new Movebox(hitbox));
        data.set(entity, new GameCharacter(name, physicParams, hurtParams));
        data.set(entity, new Speed(0, 0));
        data.set(entity, new BehaviourKey(name + BEHAVIOUR_IDLE, world.getTick()));
        data.set(entity, FacingDirection.RIGHT);
        return entity;
    }


    private static RenderTask createRenderTask(Etherworld world, Function<String, SpriteData> spriteMap, Function<ChunkPosition, SpriteData> chunkMap, int cameraPerson, PositionConverter converter) {
        int cameraWidth = 800;
        int cameraHeight = 400;
        int cameraOffsetX = 0 - cameraWidth / 2;
        int cameraOffsetY = -72 - cameraHeight / 2;
        List<RenderChunk> chunks = new ArrayList<>();
        List<RenderSprite> sprites = new ArrayList<>();
        List<DebugRectangle> rectangles = new ArrayList<>();
        List<String> lines = new ArrayList<>();
        EntityData data = world.getData();

        PixelPosition cameraPersonPosition = converter.floorPixel(data.get(cameraPerson, Position.class));
        RenderRectangle camera = new RenderRectangle(
                cameraPersonPosition.x() + cameraOffsetX,
                cameraPersonPosition.y() + cameraOffsetY,
                cameraWidth,
                cameraHeight);
        rectangles.add(new DebugRectangle(camera, Color.WHITE, false));

        ChunkPosition cameraMin = converter.floorChunk(new PixelPosition(camera.aX(), camera.aY()));
        ChunkPosition cameraMax = converter.ceilChunk(new PixelPosition(camera.bX(), camera.bY()));
        for (int y = cameraMin.y(); y < cameraMax.y(); y++) {
            for (int x = cameraMin.x(); x < cameraMax.x(); x++) {
                ChunkPosition point = new ChunkPosition(x, y);
                SpriteData chunk = chunkMap.apply(point);
                if (chunk != null) {
                    AseFrame hitboxFrame = chunk.info.frames().stream().filter(t -> t.filename().equals("Hitbox")).findFirst().get();
                    AseFrame backgroundFrame = chunk.info.frames().stream().filter(t -> t.filename().equals("Background")).findFirst().get();
                    AseFrame foregroundFrame = chunk.info.frames().stream().filter(t -> t.filename().equals("Foreground")).findFirst().get();
                    PixelPosition pixelPosition = converter.toPixel(point);
                    RenderRectangle dest = new RenderRectangle(
                            pixelPosition.x(),
                            pixelPosition.y(),
                            converter.getTileSize() * converter.getChunkSize().x(),
                            converter.getTileSize() * converter.getChunkSize().y());

                    chunks.add(new RenderChunk(
                            new RenderRectangle(backgroundFrame.frame().x(), backgroundFrame.frame().y(), backgroundFrame.frame().w(), backgroundFrame.frame().h()),
                            new RenderRectangle(hitboxFrame.frame().x(), hitboxFrame.frame().y(), hitboxFrame.frame().w(), hitboxFrame.frame().h()),
                            new RenderRectangle(foregroundFrame.frame().x(), foregroundFrame.frame().y(), foregroundFrame.frame().w(), foregroundFrame.frame().h()),
                            dest,
                            chunk.image
                    ));
                    rectangles.add(new DebugRectangle(dest, Color.LIGHT_GRAY, false));
                }
            }
        }

        for (int entity : data.list(MovingPlatform.class)) {
            String platformId = data.get(entity, MovingPlatform.class).id();
            SpriteData spriteData = spriteMap.apply(platformId);
            Position position = data.get(entity, Position.class);
            PixelPosition pixelPosition = converter.floorPixel(position);
            int activeFrameIndex = 0;
            AseSlice hitboxSlice = spriteData.info.meta().slices().stream().filter(x -> x.name().equals("Obstacle")).findFirst().get();
            AseSliceKey hitboxKey = hitboxSlice.keys().get(0);
            PixelPosition spriteOffset = new PixelPosition(
                    -hitboxKey.bounds().x() - hitboxKey.pivot().x(),
                    -hitboxKey.bounds().y() - hitboxKey.pivot().y());

            AseFrame activeFrame = spriteData.info.frames().get(activeFrameIndex);
            RenderRectangle dest = new RenderRectangle(
                    activeFrame.spriteSourceSize().x() + spriteOffset.x() + pixelPosition.x(),
                    activeFrame.spriteSourceSize().y() + spriteOffset.y() + pixelPosition.y(),
                    activeFrame.spriteSourceSize().w(),
                    activeFrame.spriteSourceSize().h());
            FacingDirection direction = data.get(entity, FacingDirection.class);
            if (direction == FacingDirection.LEFT) {
                dest = dest.mirrorX(pixelPosition.x());
            }
            RenderRectangle source = new RenderRectangle(
                    activeFrame.frame().x(),
                    activeFrame.frame().y(),
                    activeFrame.frame().w(),
                    activeFrame.frame().h());
            if (dest.intersects(camera)) {
                sprites.add(new RenderSprite(source, dest, spriteData.image));
            }
        }

        for (int entity : data.list(GameCharacter.class)) {
            String characterId = data.get(entity, GameCharacter.class).id();
            SpriteData spriteData = spriteMap.apply(characterId);
            Position position = data.get(entity, Position.class);
            PixelPosition pixelPosition = converter.floorPixel(position);

            int activeFrameIndex;
            BehaviourKey animation = data.get(entity, BehaviourKey.class);
            if (animation != null) {
                int ticks = (int) (world.getTick() - animation.startTick());
                activeFrameIndex = spriteData.info.frameIndexByMillis(convert(data, entity, animation.value()), ticks * MILLIS_PER_SECOND / TICKS_PER_SECOND);
            } else {
                activeFrameIndex = 0;
            }
            AseSlice hitboxSlice = spriteData.info.meta().slices().stream().filter(x -> x.name().equals("Hitbox")).findFirst().get();
            AseSliceKey hitboxKey = hitboxSlice.keys().get(0);
            PixelPosition spriteOffset = new PixelPosition(
                    -hitboxKey.bounds().x() - hitboxKey.pivot().x(),
                    -hitboxKey.bounds().y() - hitboxKey.pivot().y());

            AseFrame activeFrame = spriteData.info.frames().get(activeFrameIndex);
            RenderRectangle dest = new RenderRectangle(
                    activeFrame.spriteSourceSize().x() + spriteOffset.x() + pixelPosition.x(),
                    activeFrame.spriteSourceSize().y() + spriteOffset.y() + pixelPosition.y(),
                    activeFrame.spriteSourceSize().w(),
                    activeFrame.spriteSourceSize().h());
            FacingDirection direction = data.get(entity, FacingDirection.class);
            if (direction == FacingDirection.LEFT) {
                dest = dest.mirrorX(pixelPosition.x());
            }
            RenderRectangle source = new RenderRectangle(
                    activeFrame.frame().x(),
                    activeFrame.frame().y(),
                    activeFrame.frame().w(),
                    activeFrame.frame().h());
            if (dest.intersects(camera)) {
                sprites.add(new RenderSprite(source, dest, spriteData.image));
            }
        }

        for (int entity : data.list(Obstaclebox.class)) {
            Obstaclebox box = data.get(entity, Obstaclebox.class);
            Position position = data.get(entity, Position.class);
            FacingDirection facing = data.get(entity, FacingDirection.class);
            RenderRectangle pixelHitbox = toPixelHitbox(converter, box.hitbox(), position, facing);
            if (pixelHitbox.intersects(camera)) {
                rectangles.add(new DebugRectangle(pixelHitbox, Color.BLACK, false));
            }
        }
        for (int entity : data.list(Hurtbox.class)) {
            Hurtbox box = data.get(entity, Hurtbox.class);
            Position position = data.get(entity, Position.class);
            FacingDirection facing = data.get(entity, FacingDirection.class);
            RenderRectangle pixelHitbox = toPixelHitbox(converter, box.hitbox(), position, facing);
            if (pixelHitbox.intersects(camera)) {
                rectangles.add(new DebugRectangle(pixelHitbox, Color.BLUE, false));
            }
        }
        for (int entity : data.list(Attackbox.class)) {
            Attackbox box = data.get(entity, Attackbox.class);
            Position position = data.get(entity, Position.class);
            FacingDirection facing = data.get(entity, FacingDirection.class);
            RenderRectangle pixelHitbox = toPixelHitbox(converter, box.hitbox(), position, facing);
            if (pixelHitbox.intersects(camera)) {
                rectangles.add(new DebugRectangle(pixelHitbox, Color.RED, false));
            }
        }

        return new RenderTask(world.getTick(), Color.DARK_GRAY, camera, chunks, sprites, rectangles, lines);
    }

    private static RenderRectangle toPixelHitbox(PositionConverter converter, RectangleHitbox hitbox, Position position, FacingDirection facing) {
        if (facing == FacingDirection.LEFT) {
            hitbox = hitbox.mirrorX(0);
        }
        hitbox = hitbox.translate(position.x(), position.y());
        return new RenderRectangle(
                converter.positionToFloorPixel(hitbox.x()),
                converter.positionToFloorPixel(hitbox.y()),
                converter.positionToFloorPixel(hitbox.width()),
                converter.positionToFloorPixel(hitbox.height())
        );
    }

    private static String convert(EntityData data, int entity, String state) {
        if (state.endsWith(BEHAVIOUR_IDLE)) {
            Speed speed = data.get(entity, Speed.class);
            if (speed == null) {
                speed = new Speed(0, 0);
            }
            if (speed.y() > 0) {
                return "Down";
            }
            if (speed.y() < 0) {
                return "Up";
            }
            if (speed.x() != 0) {
                return "Run";
            }
            return "Stand";
        }
        if (state.endsWith(BEHAVIOUR_ATTACK)) {
            return "Attack";
        }
        if (state.endsWith(BEHAVIOUR_HURT)) {
            return "Hit";
        }
        if (state.endsWith(BEHAVIOUR_DEAD)) {
            return "Dead";
        }
        throw new AssertionError(state);
    }

    private static Chunk convert(SpriteData spriteData, PositionConverter converter) {
        AseFrame hitboxFrame = spriteData.info.frames().stream().filter(t -> t.filename().equals("Hitbox")).findFirst().get();
        Chunk chunk = new Chunk(converter.getChunkSize());

        for (int y = 0; y < converter.getChunkSize().y(); y++) {
            for (int x = 0; x < converter.getChunkSize().x(); x++) {
                LocalTilePosition localTilePosition = new LocalTilePosition(x, y);
                PixelPosition pixelPosition = converter.toPixel(localTilePosition);
                Color color = new Color(spriteData.image.getRGB(
                        hitboxFrame.frame().x() + pixelPosition.x(),
                        hitboxFrame.frame().y() + pixelPosition.y()),
                        true);
                chunk.setObstacle(localTilePosition, color.getAlpha() != 0);
            }
        }
        return chunk;
    }
}
