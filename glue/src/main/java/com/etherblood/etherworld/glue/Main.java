package com.etherblood.etherworld.glue;

import com.etherblood.etherworld.data.EntityData;
import com.etherblood.etherworld.data.EntityDatabase;
import com.etherblood.etherworld.engine.Etherworld;
import com.etherblood.etherworld.engine.GameLoop;
import com.etherblood.etherworld.engine.PlayerAction;
import com.etherblood.etherworld.engine.PositionConverter;
import com.etherblood.etherworld.engine.chunks.Chunk;
import com.etherblood.etherworld.engine.chunks.ChunkManager;
import com.etherblood.etherworld.engine.chunks.ChunkPosition;
import com.etherblood.etherworld.engine.chunks.LocalTilePosition;
import com.etherblood.etherworld.engine.chunks.PixelPosition;
import com.etherblood.etherworld.engine.components.Animation;
import com.etherblood.etherworld.engine.components.CharacterId;
import com.etherblood.etherworld.engine.components.Direction;
import com.etherblood.etherworld.engine.components.OwnerId;
import com.etherblood.etherworld.engine.components.Position;
import com.etherblood.etherworld.engine.components.Speed;
import com.etherblood.etherworld.engine.sprites.GameSprite;
import com.etherblood.etherworld.engine.sprites.GameSpriteAnimation;
import com.etherblood.etherworld.engine.sprites.GameSpriteFrame;
import com.etherblood.etherworld.engine.sprites.GameSpriteHitbox;
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
import com.etherblood.etherworld.spriteloader.aseprite.AseSprite;
import java.awt.Color;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Main {

    private static final int FPS = 60;

    public static void main(String... args) {
        AssetLoader assetLoader = new AssetLoader(
                "assets/src/main/resources/aseprite/chunks/export",
                "assets/src/main/resources/aseprite/sprites/export");

        // only chunks from this set can be loaded
        // TODO: create world/map file with chunkPosition -> chunkFile mapping
        Set<ChunkPosition> worldChunks = Set.of(
                new ChunkPosition(-1, 0),
                new ChunkPosition(0, 0),
                new ChunkPosition(1, 0),
                new ChunkPosition(2, 0),
                new ChunkPosition(2, 1)
        );

        EntityDatabase data = new EntityDatabase();
        PositionConverter converter = new PositionConverter();
        Etherworld world = new Etherworld(
                data,
                name -> convert(name, assetLoader.loadSprite(name).info, converter, FPS),
                converter,
                new ChunkManager(position -> worldChunks.contains(position) ? convert(assetLoader.loadChunk(position), converter) : null));

        int player = data.createEntity();
        int tabby = data.createEntity();
        data.set(tabby, new OwnerId(player));
        data.set(tabby, new CharacterId("Tabby"));
        data.set(tabby, Direction.RIGHT);
        data.set(tabby, new Animation("Tabby", "Stand", 0));
        data.set(tabby, new Position(0, 0));
        data.set(tabby, new Speed(0, 0));

        Gui gui = new Gui();
        gui.start();
        gui.render(createRenderTask(world, assetLoader::loadSprite, position -> worldChunks.contains(position) ? assetLoader.loadChunk(position) : null, tabby));

        Map<Integer, PlayerAction> actionMappings = Map.of(
                KeyEvent.VK_LEFT, PlayerAction.LEFT,
                KeyEvent.VK_UP, PlayerAction.JUMP,
                KeyEvent.VK_RIGHT, PlayerAction.RIGHT,
                KeyEvent.VK_A, PlayerAction.ATTACK
        );

        GameLoop loop = new GameLoop(FPS, () -> {
            world.tick(Map.of(player, gui.getPressedKeys().stream().map(actionMappings::get).collect(Collectors.toSet())));
            gui.render(createRenderTask(world, assetLoader::loadSprite, position -> worldChunks.contains(position) ? assetLoader.loadChunk(position) : null, tabby));
        });
        loop.run();
    }


    private static RenderTask createRenderTask(Etherworld world, Function<String, SpriteData> spriteMap, Function<ChunkPosition, SpriteData> chunkMap, int cameraPerson) {
        int cameraWidth = 800;
        int cameraHeight = 400;
        int cameraOffsetX = 0 - cameraWidth / 2;
        int cameraOffsetY = -72 - cameraHeight / 2;
        RenderRectangle camera = new RenderRectangle(cameraOffsetX, cameraOffsetY, cameraWidth, cameraHeight);
        List<RenderChunk> chunks = new ArrayList<>();
        List<RenderSprite> sprites = new ArrayList<>();
        List<DebugRectangle> rectangles = new ArrayList<>();
        List<String> lines = new ArrayList<>();

        EntityData data = world.getData();
        for (int character : data.list(CharacterId.class)) {
            SpriteData spriteData = spriteMap.apply(data.get(character, CharacterId.class).id());
            GameSprite gameSprite = world.getSprites().apply(data.get(character, CharacterId.class).id());
            Position position = data.get(character, Position.class);
            PixelPosition pixelPosition = world.getConverter().floorPixel(position);
//            sprites.add(new RenderSprite())
//            lines.add(pixelPosition.toString());

            int activeFrameIndex = 0;
            Animation animation = data.get(character, Animation.class);
            if (animation != null) {
                int ticks = animation.elapsedTicks();
                GameSpriteAnimation gameSpriteAnimation = gameSprite.animations().get(animation.animationId());
                for (GameSpriteFrame frame : gameSpriteAnimation.frames()) {
                    activeFrameIndex = frame.index();
                    ticks -= frame.durationTicks();
                    if (ticks <= 0) {
                        break;
                    }
                }
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
            if (data.get(character, Direction.class) == Direction.LEFT) {
                dest = new RenderRectangle(
                        2 * pixelPosition.x() - dest.x(),
                        dest.y(),
                        -dest.width(),
                        dest.height()
                );
            }
            RenderRectangle source = new RenderRectangle(
                    activeFrame.frame().x(),
                    activeFrame.frame().y(),
                    activeFrame.frame().w(),
                    activeFrame.frame().h());
            sprites.add(new RenderSprite(source, dest, spriteData.image));

            // TODO: render hitbox
            if (character == cameraPerson) {
                camera = new RenderRectangle(
                        pixelPosition.x() + cameraOffsetX,
                        pixelPosition.y() + cameraOffsetY,
                        cameraWidth,
                        cameraHeight);
                rectangles.add(new DebugRectangle(camera, Color.WHITE, false));
            }
        }

        ChunkPosition cameraMin = world.getConverter().floorChunk(new PixelPosition(camera.minX(), camera.minY()));
        ChunkPosition cameraMax = world.getConverter().ceilChunk(new PixelPosition(camera.maxX(), camera.maxY()));
        for (int y = cameraMin.y(); y < cameraMax.y(); y++) {
            for (int x = cameraMin.x(); x < cameraMax.x(); x++) {
                ChunkPosition point = new ChunkPosition(x, y);
                SpriteData chunk = chunkMap.apply(point);
                if (chunk != null) {
                    AseFrame hitboxFrame = chunk.info.frames().stream().filter(t -> t.filename().equals("Hitbox")).findFirst().get();
                    AseFrame backgroundFrame = chunk.info.frames().stream().filter(t -> t.filename().equals("Background")).findFirst().get();
                    AseFrame foregroundFrame = chunk.info.frames().stream().filter(t -> t.filename().equals("Foreground")).findFirst().get();
                    PixelPosition pixelPosition = world.getConverter().toPixel(point);
                    RenderRectangle dest = new RenderRectangle(
                            pixelPosition.x(),
                            pixelPosition.y(),
                            world.getConverter().getTileSize() * world.getConverter().getChunkSize().x(),
                            world.getConverter().getTileSize() * world.getConverter().getChunkSize().y());
//                    graphics.setColor(Color.WHITE);
//                    graphics.drawImage(
//                            chunk.sprite.image,
//                            dest.x(), dest.y(), dest.x() + dest.width(), dest.y() + dest.height(),
//                            hitboxFrame.frame.x, hitboxFrame.frame.y, hitboxFrame.frame.x + hitboxFrame.frame.w, hitboxFrame.frame.y + hitboxFrame.frame.h,
//                            null);
//
//                    graphics.setColor(Color.BLUE);
//                    graphics.drawRect(dest.x(), dest.y(), dest.width(), dest.height());
//
//                    visibleChunks.put(point, chunk);
                    chunks.add(new RenderChunk(
                            new RenderRectangle(backgroundFrame.frame().x(), backgroundFrame.frame().y(), backgroundFrame.frame().w(), backgroundFrame.frame().h()),
                            new RenderRectangle(hitboxFrame.frame().x(), hitboxFrame.frame().y(), hitboxFrame.frame().w(), hitboxFrame.frame().h()),
                            new RenderRectangle(foregroundFrame.frame().x(), foregroundFrame.frame().y(), foregroundFrame.frame().w(), foregroundFrame.frame().h()),
                            dest,
                            chunk.image
                    ));
                    rectangles.add(new DebugRectangle(dest, Color.BLUE, false));
                }
            }
        }

//        lines.add(camera.toString());
        return new RenderTask(camera, chunks, sprites, rectangles, lines);
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

    private static GameSprite convert(String id, AseSprite sprite, PositionConverter converter, int fps) {
        AseSlice hitboxSlice = sprite.meta().slices().stream().filter(x -> x.name().equals("Hitbox")).findFirst().get();
        AseSliceKey hitboxKey = hitboxSlice.keys().get(0);
        RenderRectangle pixelHitbox = new RenderRectangle(
                -hitboxKey.pivot().x(),
                -hitboxKey.pivot().y(),
                hitboxKey.bounds().w(),
                hitboxKey.bounds().h());
        GameSpriteHitbox hitbox = new GameSpriteHitbox(
                converter.pixelToPosition(pixelHitbox.x()),
                converter.pixelToPosition(pixelHitbox.y()),
                converter.pixelToPosition(pixelHitbox.width()),
                converter.pixelToPosition(pixelHitbox.height()));

        Map<String, GameSpriteAnimation> animations = new HashMap<>();
        Set<String> tagNames = sprite.meta().frameTags().stream().map(x -> x.name()).collect(Collectors.toSet());
        for (String tagName : tagNames) {
            int[] frameIndices = sprite.meta().frameTags().stream()
                    .filter(x -> x.name().equals(tagName))
                    .flatMapToInt(x -> IntStream.rangeClosed(x.from(), x.to()))
                    .toArray();
            List<GameSpriteFrame> frames = new ArrayList<>();
            for (int frameIndex : frameIndices) {
                AseFrame aseFrame = sprite.frames().get(frameIndex);
                int ticks = Math.round(fps * aseFrame.duration() / 1000f);
                List<AseSliceKey> damageKeys = sprite.meta().slices().stream()
                        .filter(x -> x.name().equalsIgnoreCase("Damage"))
                        .flatMap(x -> x.keys().stream())
                        .filter(key -> key.frame() == frameIndex)
                        .toList();
                GameSpriteHitbox[] attackHitboxes = damageKeys.stream()
                        .map(x -> new GameSpriteHitbox(
                                converter.pixelToPosition(x.bounds().x()),
                                converter.pixelToPosition(x.bounds().y()),
                                converter.pixelToPosition(x.bounds().w()),
                                converter.pixelToPosition(x.bounds().h())))
                        .toArray(GameSpriteHitbox[]::new);
                frames.add(new GameSpriteFrame(frameIndex, ticks, attackHitboxes));
            }
            animations.put(tagName, new GameSpriteAnimation(frames.toArray(GameSpriteFrame[]::new)));
        }
        //character.spriteOffset = new PixelPoint(-hitboxKey.bounds.x - hitboxKey.pivot.x, -hitboxKey.bounds.y - hitboxKey.pivot.y);

        return new GameSprite(id, animations, hitbox);
    }
}
