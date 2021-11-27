package com.etherblood.etherworld.glue;

import com.etherblood.etherworld.data.EntityData;
import com.etherblood.etherworld.data.EntityDatabase;
import com.etherblood.etherworld.engine.Etherworld;
import com.etherblood.etherworld.engine.GameLoop;
import com.etherblood.etherworld.engine.PlayerAction;
import com.etherblood.etherworld.engine.PositionConverter;
import com.etherblood.etherworld.engine.RectangleHitbox;
import com.etherblood.etherworld.engine.characters.CharacterParams;
import com.etherblood.etherworld.engine.characters.CharacterState;
import com.etherblood.etherworld.engine.characters.CharacterSystem;
import com.etherblood.etherworld.engine.characters.components.AttackParams;
import com.etherblood.etherworld.engine.characters.components.CharacterStateKey;
import com.etherblood.etherworld.engine.characters.components.CrouchParams;
import com.etherblood.etherworld.engine.characters.components.HurtParams;
import com.etherblood.etherworld.engine.characters.components.PhysicParams;
import com.etherblood.etherworld.engine.chunks.Chunk;
import com.etherblood.etherworld.engine.chunks.ChunkManager;
import com.etherblood.etherworld.engine.chunks.ChunkPosition;
import com.etherblood.etherworld.engine.chunks.LocalTilePosition;
import com.etherblood.etherworld.engine.chunks.PixelPosition;
import com.etherblood.etherworld.engine.components.Attackbox;
import com.etherblood.etherworld.engine.components.FacingDirection;
import com.etherblood.etherworld.engine.components.GameCharacter;
import com.etherblood.etherworld.engine.components.Health;
import com.etherblood.etherworld.engine.components.Hurtbox;
import com.etherblood.etherworld.engine.components.Movebox;
import com.etherblood.etherworld.engine.components.MovingPlatform;
import com.etherblood.etherworld.engine.components.Obstaclebox;
import com.etherblood.etherworld.engine.components.OnGround;
import com.etherblood.etherworld.engine.components.OwnerId;
import com.etherblood.etherworld.engine.components.Position;
import com.etherblood.etherworld.engine.components.Respawn;
import com.etherblood.etherworld.engine.components.Speed;
import com.etherblood.etherworld.engine.golem.GolemHandState;
import com.etherblood.etherworld.engine.golem.GolemHandSystem;
import com.etherblood.etherworld.engine.golem.GolemHeadState;
import com.etherblood.etherworld.engine.golem.GolemHeadSystem;
import com.etherblood.etherworld.engine.golem.components.GolemHand;
import com.etherblood.etherworld.engine.golem.components.GolemHandStateKey;
import com.etherblood.etherworld.engine.golem.components.GolemHeadStateKey;
import com.etherblood.etherworld.engine.systems.MovementSystem;
import com.etherblood.etherworld.engine.systems.MovingPlatformSystem;
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
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.imageio.ImageIO;

class Main {

    private static final int TICKS_PER_SECOND = 60;
    private static final int MILLIS_PER_SECOND = 1000;

    public static void main(String... args) throws IOException {
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
        EntityDatabase data = new EntityDatabase();
        PositionConverter converter = new PositionConverter();
        ChunkManager chunks = new ChunkManager(
                converter.getTileSize() * converter.getPixelSize(),
                converter.getChunkSize(),
                position -> worldChunks.contains(position) ? convert(assetLoader.loadChunk(position), converter) : null);
        Map<String, CharacterParams> characterParams = new HashMap<>();
        Etherworld world = new Etherworld(
                data,
                chunks,
                List.of(
                        new CharacterSystem(characterParams),
                        new GolemHeadSystem(),
                        new GolemHandSystem(),
                        new MovingPlatformSystem(),
                        new MovementSystem()
                )
        );

        int player = data.createEntity();
        int tabby = createCharacter(world, assetLoader, converter, characterParams, "Tabby", data.createEntity());
        data.set(tabby, new OwnerId(player));
        data.set(tabby, FacingDirection.RIGHT);
        data.set(tabby, new Position(0, 23 * 16 * 16));
        data.set(tabby, new Respawn(data.get(tabby, Position.class)));
        data.set(tabby, new Health(5, 5));

        int slime = createCharacter(world, assetLoader, converter, characterParams, "Slime", data.createEntity());
        data.set(slime, FacingDirection.LEFT);
        data.set(slime, new Position(800 * converter.getPixelSize(), 24 * 16 * 16));
        data.set(slime, new Respawn(data.get(slime, Position.class)));
        data.set(slime, new Health(2, 2));
        data.set(slime, new Attackbox(data.get(slime, Hurtbox.class).hitbox(), 1));

        int dummy = createCharacter(world, assetLoader, converter, characterParams, "Tabby", data.createEntity());
        data.set(dummy, FacingDirection.LEFT);
        data.set(dummy, new Position(896 * converter.getPixelSize(), 0));
        data.set(dummy, new Health(10, 10));

        int amara = createCharacter(world, assetLoader, converter, characterParams, "Amara", data.createEntity());
        data.set(amara, FacingDirection.LEFT);
        data.set(amara, new Position(944 * converter.getPixelSize(), 24 * 16 * 16));

        int fallacia = createCharacter(world, assetLoader, converter, characterParams, "Fallacia", data.createEntity());
        data.set(fallacia, FacingDirection.LEFT);
        data.set(fallacia, new Position(992 * converter.getPixelSize(), 24 * 16 * 16));

        int furor = createCharacter(world, assetLoader, converter, characterParams, "Furor", data.createEntity());
        data.set(furor, FacingDirection.LEFT);
        data.set(furor, new Position(1104 * converter.getPixelSize(), 24 * 16 * 16));

        {
            int head = data.createEntity();
            {
                String name = "GolemHead";
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

                data.set(head, new Position(2560 * converter.getPixelSize(), 720 * converter.getPixelSize()));
                data.set(head, new GameCharacter(name));
                data.set(head, new HurtParams(1 * TICKS_PER_SECOND, 0));// TODO: read value from file

                data.set(head, new Health(9, 9));
                data.set(head, new Hurtbox(hitbox));
                data.set(head, new Obstaclebox(hitbox));
                data.set(head, new Speed(0, 0));
                data.set(head, new GolemHeadStateKey(GolemHeadState.IDLE, 0));
            }

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

            int leftHand = data.createEntity();
            data.set(leftHand, new Position(2760 * converter.getPixelSize(), 832 * converter.getPixelSize()));
            data.set(leftHand, new Movebox(hitbox));
            data.set(leftHand, new Obstaclebox(hitbox));
            data.set(leftHand, new GameCharacter(name));
            data.set(leftHand, new GolemHand(head));
            data.set(leftHand, new GolemHandStateKey(GolemHandState.RESET, 0));
            data.set(leftHand, FacingDirection.RIGHT);

            int rightHand = data.createEntity();
            data.set(rightHand, new Position(2360 * converter.getPixelSize(), 832 * converter.getPixelSize()));
            data.set(rightHand, new Movebox(hitbox));
            data.set(rightHand, new Obstaclebox(hitbox));
            data.set(rightHand, new GameCharacter(name));
            data.set(rightHand, new GolemHand(head));
            data.set(rightHand, FacingDirection.LEFT);
            data.set(rightHand, new GolemHandStateKey(GolemHandState.RESET, 0));
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
        gui.start(e -> {
            switch (e.getKeyCode()) {
                case KeyEvent.VK_F2 -> {
                    List<String> availableCharacters = List.of("Tabby", "Amara", "Fallacia", "Furor", "Slime", "amazing_wolf");
                    for (int entity : data.findByValue(new OwnerId(player))) {
                        GameCharacter gameCharacter = data.get(entity, GameCharacter.class);
                        if (gameCharacter != null) {
                            int index = availableCharacters.indexOf(gameCharacter.id());
                            int next = (index + 1) % availableCharacters.size();

                            // hackish solution, we hope everything from previous character gets overwritten...
                            createCharacter(world, assetLoader, converter, characterParams, availableCharacters.get(next), entity);
                        }
                    }
                }
                case KeyEvent.VK_F5 -> {
                    assetLoader.clear();
                    chunks.clear();
                }
                case KeyEvent.VK_ESCAPE -> data.set(tabby, new CharacterStateKey(CharacterState.DEAD, -999999));
            }
        }, ImageIO.read(new File("assets/icons/Tabby.png")));
        gui.render(createRenderTask(world, assetLoader::loadSprite, position -> worldChunks.contains(position) ? assetLoader.loadChunk(position) : null, tabby, converter));

        Map<Integer, PlayerAction> actionMappings = Map.of(
                KeyEvent.VK_LEFT, PlayerAction.LEFT,
                KeyEvent.VK_UP, PlayerAction.JUMP,
                KeyEvent.VK_DOWN, PlayerAction.CROUCH,
                KeyEvent.VK_RIGHT, PlayerAction.RIGHT,
                KeyEvent.VK_A, PlayerAction.ATTACK
        );

        GameLoop loop = new GameLoop(TICKS_PER_SECOND, () -> {
            world.tick(Map.of(player, gui.getPressedKeys().stream().map(actionMappings::get).collect(Collectors.toSet())));
            gui.render(createRenderTask(world, assetLoader::loadSprite, position -> worldChunks.contains(position) ? assetLoader.loadChunk(position) : null, tabby, converter));
        });
        loop.run();
    }

    private static int createCharacter(Etherworld world, AssetLoader assetLoader, PositionConverter converter, Map<String, CharacterParams> characterParams, String name, int entity) {
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

        AttackParams attackParams = null;
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

            if (startMillis != null) {
                if (endMillis == null) {
                    endMillis = millis - 1;
                }
                attackParams = new AttackParams(
                        damagebox,
                        startMillis * TICKS_PER_SECOND / MILLIS_PER_SECOND,
                        endMillis * TICKS_PER_SECOND / MILLIS_PER_SECOND + 1,
                        1,
                        sprite.info.animationDurationMillis("Attack") * TICKS_PER_SECOND / MILLIS_PER_SECOND);
            }
        }
        Optional<AseSlice> crouchSlice = sprite.info.meta().slices().stream().filter(x -> x.name().equals("Crouch")).findFirst();
        CrouchParams crouchParams = crouchSlice.map(aseSlice -> {
            AseSliceKey aseSliceKey = aseSlice.keys().get(0);
            Position p = new Position(
                    converter.pixelToPosition(hitboxKey.pivot().x() + hitboxKey.bounds().x()),
                    converter.pixelToPosition(hitboxKey.pivot().y() + hitboxKey.bounds().y()));
            return new CrouchParams(new RectangleHitbox(
                    converter.pixelToPosition(aseSliceKey.bounds().x()) - p.x(),
                    converter.pixelToPosition(aseSliceKey.bounds().y()) - p.y(),
                    converter.pixelToPosition(aseSliceKey.bounds().w()),
                    converter.pixelToPosition(aseSliceKey.bounds().h())));
        }).orElse(null);

        PhysicParams physicParams = new PhysicParams(hitbox, 8 * 16, 20, 12 * 16, 12);
        HurtParams hurtParams = new HurtParams(
                sprite.info.animationDurationMillis("Hit") * TICKS_PER_SECOND / MILLIS_PER_SECOND,
                5 * TICKS_PER_SECOND);
        data.set(entity, new Hurtbox(hitbox));
        data.set(entity, new Movebox(hitbox));
        data.set(entity, new GameCharacter(name));
        data.set(entity, new Speed(0, 0));
        data.set(entity, new CharacterStateKey(CharacterState.IDLE, world.getTick()));
        data.set(entity, FacingDirection.RIGHT);
        if (!characterParams.containsKey(name)) {
            CharacterParams params = new CharacterParams(physicParams, attackParams, hurtParams, crouchParams);
            characterParams.put(name, params);
        }
        return entity;
    }


    private static RenderTask createRenderTask(Etherworld world, Function<String, SpriteData> spriteMap, Function<ChunkPosition, SpriteData> chunkMap, int cameraPerson, PositionConverter converter) {
        int scale = 2;
        int cameraWidth = 640;
        int cameraHeight = 360;
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

            int activeFrameIndex = getFrameIndex(world, spriteData, entity);
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

        return new RenderTask(scale, world.getTick(), Color.GRAY, camera, chunks, sprites, rectangles, lines);
    }

    private static RenderRectangle toPixelHitbox(PositionConverter converter, RectangleHitbox hitbox, Position position, FacingDirection facing) {
        if (facing == FacingDirection.LEFT) {
            hitbox = hitbox.mirrorX(0);
        }
        hitbox = hitbox.translate(position);
        return new RenderRectangle(
                converter.positionToFloorPixel(hitbox.x()),
                converter.positionToFloorPixel(hitbox.y()),
                converter.positionToFloorPixel(hitbox.width()),
                converter.positionToFloorPixel(hitbox.height())
        );
    }

    private static int getFrameIndex(Etherworld world, SpriteData spriteData, int entity) {
        EntityData data = world.getData();
        String animation = null;
        long ticks = 0;
        CharacterStateKey characterStateKey = data.get(entity, CharacterStateKey.class);
        if (characterStateKey != null) {
            animation = getAnimation(data, entity, characterStateKey.value());
            ticks = world.getTick() - characterStateKey.startTick();
        }

        GolemHeadStateKey golemHeadStateKey = data.get(entity, GolemHeadStateKey.class);
        if (golemHeadStateKey != null) {
            animation = getAnimation(data, entity, golemHeadStateKey.value());
            ticks = world.getTick() - golemHeadStateKey.startTick();
        }

        GolemHandStateKey golemHandStateKey = data.get(entity, GolemHandStateKey.class);
        if (golemHandStateKey != null) {
            animation = getAnimation(data, entity, golemHandStateKey.value());
            ticks = world.getTick() - golemHandStateKey.startTick();
        }

        int activeFrameIndex;
        if (spriteData.info.hasAnimation(animation)) {
            activeFrameIndex = spriteData.info.frameIndexByMillis(animation, (int) (ticks * MILLIS_PER_SECOND / TICKS_PER_SECOND));
        } else {
            activeFrameIndex = 0;
        }
        return activeFrameIndex;
    }

    private static String getAnimation(EntityData data, int entity, CharacterState state) {
        switch (state) {
            case IDLE -> {
                Speed speed = data.get(entity, Speed.class);
                if (speed == null) {
                    speed = new Speed(0, 0);
                }
                if (data.has(entity, OnGround.class)) {
                    if (speed.x() != 0) {
                        return "Run";
                    }
                    return "Stand";
                }
                if (speed.y() < 0) {
                    return "Up";
                }
                return "Down";
            }
            case ATTACK -> {
                return "Attack";
            }
            case HURT -> {
                return "Hit";
            }
            case DEAD -> {
                return "Dead";
            }
            case CROUCH -> {
                return "Crouch";
            }
            default -> throw new AssertionError(state);
        }
    }

    private static String getAnimation(EntityData data, int entity, GolemHeadState state) {
        switch (state) {
            case IDLE -> {
                return "Stand";
            }
            case HURT -> {
                return "Hit";
            }
            case DEAD -> {
                return "Dead";
            }
            default -> throw new AssertionError(state);
        }
    }

    private static String getAnimation(EntityData data, int entity, GolemHandState state) {
        return "Stand";
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
