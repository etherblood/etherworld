package com.etherblood.etherworld.gui;

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
import com.etherblood.etherworld.spriteloader.SpriteData;
import com.etherblood.etherworld.spriteloader.SpriteLoader;
import com.etherblood.etherworld.spriteloader.aseprite.AseFrame;
import com.etherblood.etherworld.spriteloader.aseprite.AseSlice;
import com.etherblood.etherworld.spriteloader.aseprite.AseSliceKey;
import com.etherblood.etherworld.spriteloader.aseprite.AseSprite;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;

public class Main {

    private static boolean debug = false;
    private static long runningFrameSecond;
    private static int runningFrameCount;
    private static int frameCount;

    public static void main(String... args) throws IOException {
        int fps = 60;
        int windowWidth = 1600;
        int windowHeight = 800;

        JFrame jFrame = new JFrame("Gaem?");
        jFrame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        jFrame.setLayout(new BorderLayout());
        PictureBox panel = new PictureBox();
        panel.setBackground(Color.DARK_GRAY);
        panel.setPreferredSize(new Dimension(windowWidth, windowHeight));
        panel.setVisible(true);
        jFrame.setSize(windowWidth, windowHeight);
        jFrame.add(panel, BorderLayout.CENTER);

//        panel.addMouseListener(new MouseAdapter() {
//            @Override
//            public void mouseClicked(MouseEvent e) {
//                System.out.println("Clicked on " + e.getPoint());
//            }
//        });

        jFrame.pack();
        jFrame.setVisible(true);

        Set<Integer> pressedKeys = new HashSet<>();

        SpriteLoader loader = new SpriteLoader();
        SpriteData tabbySprite = loader.getFullSprite("assets/src/main/resources/aseprite/sprites/export", "tabby-sheet.json");
        SpriteData amaraSprite = loader.getFullSprite("assets/src/main/resources/aseprite/sprites/export", "amara-sheet.json");

        Map<ChunkPosition, SpriteData> chunks = new HashMap<>();

        Map<String, GameSprite> sprites = new HashMap<>();
        Map<String, SpriteData> spriteMap = Map.of(
                "Tabby", tabbySprite,
                "Amara", amaraSprite);
        EntityDatabase data = new EntityDatabase();
        PositionConverter converter = new PositionConverter();
        Etherworld etherworld = new Etherworld(data, sprites, converter, new ChunkManager(chunkPosition -> {
            SpriteData chunkSprite = chunks.computeIfAbsent(chunkPosition, chunkPoint -> {
                ObjectMapper mapper = new ObjectMapper();
                String filename = "(" + chunkPoint.x() + "," + chunkPoint.y() + ")-sheet.json";
                File file = Paths.get("assets/src/main/resources/aseprite/chunks/export", filename).toFile();
                if (!file.exists()) {
                    return null;
                }
                try {
                    AseSprite chunkData = mapper.readValue(file, AseSprite.class);
                    BufferedImage chunkImage = ImageIO.read(Paths.get("assets/src/main/resources/aseprite/chunks/export", chunkData.meta.image).toFile());
                    return new SpriteData(chunkData, chunkImage);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
            if (chunkSprite == null) {
                return null;
            }

            AseFrame hitboxFrame = chunkSprite.info.frames.stream().filter(t -> t.filename().equals("Hitbox")).findFirst().get();
            Chunk chunk = new Chunk(converter.getChunkSize());

            for (int y = 0; y < converter.getChunkSize().y(); y++) {
                for (int x = 0; x < converter.getChunkSize().x(); x++) {
                    LocalTilePosition localTilePosition = new LocalTilePosition(x, y);
                    PixelPosition pixelPosition = converter.toPixel(localTilePosition);
                    Color color = new Color(chunkSprite.image.getRGB(
                            hitboxFrame.frame().x + pixelPosition.x(),
                            hitboxFrame.frame().y + pixelPosition.y()),
                            true);
                    chunk.setObstacle(localTilePosition, color.getAlpha() != 0);
                }
            }
            return chunk;
        }));
        sprites.put("Tabby", convert("Tabby", tabbySprite.info, etherworld.getConverter(), fps));
        sprites.put("Amara", convert("Amara", amaraSprite.info, etherworld.getConverter(), fps));
        int player = data.createEntity();
        int tabby = data.createEntity();
        data.set(tabby, new OwnerId(player));
        data.set(tabby, new CharacterId("Tabby"));
        data.set(tabby, Direction.RIGHT);
        data.set(tabby, new Animation("Tabby", "Stand", 0));
        data.set(tabby, new Position(0, 0));
        data.set(tabby, new Speed(0, 0));

        jFrame.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                pressedKeys.add(e.getKeyCode());
                switch (e.getKeyCode()) {
                    case KeyEvent.VK_F1 -> debug = !debug;
                    case KeyEvent.VK_R -> data.set(tabby, new Position(0, 0));
                }
            }

            @Override
            public void keyReleased(KeyEvent e) {
                pressedKeys.remove(e.getKeyCode());
            }
        });
        Map<Integer, PlayerAction> actionMappings = Map.of(
                KeyEvent.VK_LEFT, PlayerAction.LEFT,
                KeyEvent.VK_UP, PlayerAction.JUMP,
                KeyEvent.VK_RIGHT, PlayerAction.RIGHT,
                KeyEvent.VK_A, PlayerAction.ATTACK
        );

        AtomicReference<RenderTask> renderTask = new AtomicReference<>(createRenderTask(etherworld, spriteMap::get, chunks::get, tabby));
        GameLoop loop = new GameLoop(fps, () -> {
            etherworld.tick(Map.of(player, pressedKeys.stream().map(actionMappings::get).collect(Collectors.toSet())));
            renderTask.set(createRenderTask(etherworld, spriteMap::get, chunks::get, tabby));
            SwingUtilities.invokeLater(() -> {
                RenderTask task = renderTask.getAndSet(null);
                if (task == null) {
                    return;
                }
                BufferedImage image = panel.createImage();
                Graphics2D graphics = (Graphics2D) image.getGraphics();
                render(graphics, task);
                graphics.dispose();
                panel.setImage(image);
            });
        });
        loop.run();
    }

    private static void render(Graphics2D graphics, RenderTask renderTask) {
        AffineTransform transform = AffineTransform.getScaleInstance(2, 2);
        transform.translate(-renderTask.camera().x(), -renderTask.camera().y());
        graphics.setTransform(transform);
        graphics.setBackground(Color.GRAY);
        graphics.clearRect(
                renderTask.camera().x(),
                renderTask.camera().y(),
                renderTask.camera().width(),
                renderTask.camera().height());
        graphics.setColor(Color.WHITE);
        if (debug) {
            for (RenderChunk chunk : renderTask.chunks()) {
                graphics.drawImage(chunk.sheet(),
                        chunk.destination().minX(),
                        chunk.destination().minY(),
                        chunk.destination().maxX(),
                        chunk.destination().maxY(),
                        chunk.hitbox().minX(),
                        chunk.hitbox().minY(),
                        chunk.hitbox().maxX(),
                        chunk.hitbox().maxY(),
                        null);
            }
        }
        if (!debug) {
            for (RenderChunk chunk : renderTask.chunks()) {
                graphics.drawImage(chunk.sheet(),
                        chunk.destination().minX(),
                        chunk.destination().minY(),
                        chunk.destination().maxX(),
                        chunk.destination().maxY(),
                        chunk.background().minX(),
                        chunk.background().minY(),
                        chunk.background().maxX(),
                        chunk.background().maxY(),
                        null);
            }
        }
        for (RenderSprite sprite : renderTask.sprites()) {
            graphics.drawImage(sprite.sheet(),
                    sprite.destination().minX(),
                    sprite.destination().minY(),
                    sprite.destination().maxX(),
                    sprite.destination().maxY(),
                    sprite.source().minX(),
                    sprite.source().minY(),
                    sprite.source().maxX(),
                    sprite.source().maxY(),
                    null);
        }
        if (!debug) {
            for (RenderChunk chunk : renderTask.chunks()) {
                graphics.drawImage(chunk.sheet(),
                        chunk.destination().minX(),
                        chunk.destination().minY(),
                        chunk.destination().maxX(),
                        chunk.destination().maxY(),
                        chunk.foreground().minX(),
                        chunk.foreground().minY(),
                        chunk.foreground().maxX(),
                        chunk.foreground().maxY(),
                        null);
            }
        }
        if (debug) {
            for (DebugRectangle rectangle : renderTask.rectangles()) {
                if (rectangle.fill()) {
                    graphics.setColor(rectangle.color());
                    graphics.fillRect(
                            rectangle.destination().x(),
                            rectangle.destination().y(),
                            rectangle.destination().width(),
                            rectangle.destination().height());
                }
            }
            for (DebugRectangle rectangle : renderTask.rectangles()) {
                if (!rectangle.fill()) {
                    graphics.setColor(rectangle.color());
                    graphics.drawRect(
                            rectangle.destination().x(),
                            rectangle.destination().y(),
                            rectangle.destination().width(),
                            rectangle.destination().height());
                }
            }
            graphics.setColor(Color.WHITE);
            graphics.setTransform(AffineTransform.getTranslateInstance(0, 0));
            for (int i = 0; i < renderTask.lines().size(); i++) {
                graphics.drawString(renderTask.lines().get(i), 20, 20 + i * graphics.getFontMetrics().getHeight());
            }
            long frameSecond = Math.floorDiv(System.nanoTime(), 1_000_000_000L);
            runningFrameCount++;
            if (runningFrameSecond != frameSecond) {
                frameCount = runningFrameCount;
                runningFrameCount = 0;
                runningFrameSecond = frameSecond;
            }
            graphics.drawString("fps: " + frameCount, 20, 20 + renderTask.lines().size() * graphics.getFontMetrics().getHeight());
        }
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
            GameSprite gameSprite = world.getSprites().get(data.get(character, CharacterId.class).id());
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
            AseSlice hitboxSlice = spriteData.info.meta.slices.stream().filter(x -> x.name.equals("Hitbox")).findFirst().get();
            AseSliceKey hitboxKey = hitboxSlice.keys.get(0);
            PixelPosition spriteOffset = new PixelPosition(
                    -hitboxKey.bounds.x - hitboxKey.pivot.x,
                    -hitboxKey.bounds.y - hitboxKey.pivot.y);

            AseFrame activeFrame = spriteData.info.frames.get(activeFrameIndex);
            RenderRectangle dest = new RenderRectangle(
                    activeFrame.spriteSourceSize().x + spriteOffset.x() + pixelPosition.x(),
                    activeFrame.spriteSourceSize().y + spriteOffset.y() + pixelPosition.y(),
                    activeFrame.spriteSourceSize().w,
                    activeFrame.spriteSourceSize().h);
            if (data.get(character, Direction.class) == Direction.LEFT) {
                dest = new RenderRectangle(
                        2 * pixelPosition.x() - dest.x(),
                        dest.y(),
                        -dest.width(),
                        dest.height()
                );
            }
            RenderRectangle source = new RenderRectangle(
                    activeFrame.frame().x,
                    activeFrame.frame().y,
                    activeFrame.frame().w,
                    activeFrame.frame().h);
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
                    AseFrame hitboxFrame = chunk.info.frames.stream().filter(t -> t.filename().equals("Hitbox")).findFirst().get();
                    AseFrame backgroundFrame = chunk.info.frames.stream().filter(t -> t.filename().equals("Background")).findFirst().get();
                    AseFrame foregroundFrame = chunk.info.frames.stream().filter(t -> t.filename().equals("Foreground")).findFirst().get();
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
                            new RenderRectangle(backgroundFrame.frame().x, backgroundFrame.frame().y, backgroundFrame.frame().w, backgroundFrame.frame().h),
                            new RenderRectangle(hitboxFrame.frame().x, hitboxFrame.frame().y, hitboxFrame.frame().w, hitboxFrame.frame().h),
                            new RenderRectangle(foregroundFrame.frame().x, foregroundFrame.frame().y, foregroundFrame.frame().w, foregroundFrame.frame().h),
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

    private static GameSprite convert(String id, AseSprite sprite, PositionConverter converter, int fps) {
        AseSlice hitboxSlice = sprite.meta.slices.stream().filter(x -> x.name.equals("Hitbox")).findFirst().get();
        AseSliceKey hitboxKey = hitboxSlice.keys.get(0);
        RenderRectangle pixelHitbox = new RenderRectangle(
                -hitboxKey.pivot.x,
                -hitboxKey.pivot.y,
                hitboxKey.bounds.w,
                hitboxKey.bounds.h);
        GameSpriteHitbox hitbox = new GameSpriteHitbox(
                converter.pixelToPosition(pixelHitbox.x()),
                converter.pixelToPosition(pixelHitbox.y()),
                converter.pixelToPosition(pixelHitbox.width()),
                converter.pixelToPosition(pixelHitbox.height()));

        Map<String, GameSpriteAnimation> animations = new HashMap<>();
        Set<String> tagNames = sprite.meta.frameTags.stream().map(x -> x.name).collect(Collectors.toSet());
        for (String tagName : tagNames) {
            int[] frameIndices = sprite.meta.frameTags.stream()
                    .filter(x -> x.name.equals(tagName))
                    .flatMapToInt(x -> IntStream.rangeClosed(x.from, x.to))
                    .toArray();
            List<GameSpriteFrame> frames = new ArrayList<>();
            for (int frameIndex : frameIndices) {
                AseFrame aseFrame = sprite.frames.get(frameIndex);
                int ticks = Math.round(fps * aseFrame.duration() / 1000f);
                List<AseSliceKey> damageKeys = sprite.meta.slices.stream()
                        .filter(x -> x.name.equalsIgnoreCase("Damage"))
                        .flatMap(x -> x.keys.stream())
                        .filter(key -> key.frame == frameIndex)
                        .toList();
                GameSpriteHitbox[] attackHitboxes = damageKeys.stream()
                        .map(x -> new GameSpriteHitbox(
                                converter.pixelToPosition(x.bounds.x),
                                converter.pixelToPosition(x.bounds.y),
                                converter.pixelToPosition(x.bounds.w),
                                converter.pixelToPosition(x.bounds.h)))
                        .toArray(GameSpriteHitbox[]::new);
                frames.add(new GameSpriteFrame(frameIndex, ticks, attackHitboxes));
            }
            animations.put(tagName, new GameSpriteAnimation(frames.toArray(GameSpriteFrame[]::new)));
        }
        //character.spriteOffset = new PixelPoint(-hitboxKey.bounds.x - hitboxKey.pivot.x, -hitboxKey.bounds.y - hitboxKey.pivot.y);

        return new GameSprite(id, animations, hitbox);
    }
}
