package com.etherblood.etherworld.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import javax.swing.JFrame;
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

//        SpriteLoader loader = new SpriteLoader();
//        SpriteData tabbySprite = loader.getFullSprite("assets/src/main/resources/aseprite/sprites/export", "tabby-sheet.json");
//        SpriteData amaraSprite = loader.getFullSprite("assets/src/main/resources/aseprite/sprites/export", "amara-sheet.json");
//
//        Map<ChunkPosition, SpriteData> chunks = new HashMap<>();
//
//        Map<String, GameSprite> sprites = new HashMap<>();
//        Map<String, SpriteData> spriteMap = Map.of(
//                "Tabby", tabbySprite,
//                "Amara", amaraSprite);
//        EntityDatabase data = new EntityDatabase();
//        PositionConverter converter = new PositionConverter();
//        Etherworld etherworld = new Etherworld(data, sprites, converter, new ChunkManager(chunkPosition -> {
//            SpriteData chunkSprite = chunks.computeIfAbsent(chunkPosition, chunkPoint -> {
//                ObjectMapper mapper = new ObjectMapper();
//                String filename = "(" + chunkPoint.x() + "," + chunkPoint.y() + ")-sheet.json";
//                File file = Paths.get("assets/src/main/resources/aseprite/chunks/export", filename).toFile();
//                if (!file.exists()) {
//                    return null;
//                }
//                try {
//                    AseSprite chunkData = mapper.readValue(file, AseSprite.class);
//                    BufferedImage chunkImage = ImageIO.read(Paths.get("assets/src/main/resources/aseprite/chunks/export", chunkData.meta.image).toFile());
//                    return new SpriteData(chunkData, chunkImage);
//                } catch (IOException e) {
//                    throw new RuntimeException(e);
//                }
//            });
//            if (chunkSprite == null) {
//                return null;
//            }
//
//            AseFrame hitboxFrame = chunkSprite.info.frames.stream().filter(t -> t.filename().equals("Hitbox")).findFirst().get();
//            Chunk chunk = new Chunk(converter.getChunkSize());
//
//            for (int y = 0; y < converter.getChunkSize().y(); y++) {
//                for (int x = 0; x < converter.getChunkSize().x(); x++) {
//                    LocalTilePosition localTilePosition = new LocalTilePosition(x, y);
//                    PixelPosition pixelPosition = converter.toPixel(localTilePosition);
//                    Color color = new Color(chunkSprite.image.getRGB(
//                            hitboxFrame.frame().x + pixelPosition.x(),
//                            hitboxFrame.frame().y + pixelPosition.y()),
//                            true);
//                    chunk.setObstacle(localTilePosition, color.getAlpha() != 0);
//                }
//            }
//            return chunk;
//        }));
//        sprites.put("Tabby", convert("Tabby", tabbySprite.info, etherworld.getConverter(), fps));
//        sprites.put("Amara", convert("Amara", amaraSprite.info, etherworld.getConverter(), fps));
//        int player = data.createEntity();
//        int tabby = data.createEntity();
//        data.set(tabby, new OwnerId(player));
//        data.set(tabby, new CharacterId("Tabby"));
//        data.set(tabby, Direction.RIGHT);
//        data.set(tabby, new Animation("Tabby", "Stand", 0));
//        data.set(tabby, new Position(0, 0));
//        data.set(tabby, new Speed(0, 0));
//
//        jFrame.addKeyListener(new KeyAdapter() {
//            @Override
//            public void keyPressed(KeyEvent e) {
//                pressedKeys.add(e.getKeyCode());
//                switch (e.getKeyCode()) {
//                    case KeyEvent.VK_F1 -> debug = !debug;
//                    case KeyEvent.VK_R -> data.set(tabby, new Position(0, 0));
//                }
//            }
//
//            @Override
//            public void keyReleased(KeyEvent e) {
//                pressedKeys.remove(e.getKeyCode());
//            }
//        });
//        Map<Integer, PlayerAction> actionMappings = Map.of(
//                KeyEvent.VK_LEFT, PlayerAction.LEFT,
//                KeyEvent.VK_UP, PlayerAction.JUMP,
//                KeyEvent.VK_RIGHT, PlayerAction.RIGHT,
//                KeyEvent.VK_A, PlayerAction.ATTACK
//        );
//
//        AtomicReference<RenderTask> renderTask = new AtomicReference<>(createRenderTask(etherworld, spriteMap::get, chunks::get, tabby));
//        GameLoop loop = new GameLoop(fps, () -> {
//            etherworld.tick(Map.of(player, pressedKeys.stream().map(actionMappings::get).collect(Collectors.toSet())));
//            renderTask.set(createRenderTask(etherworld, spriteMap::get, chunks::get, tabby));
//            SwingUtilities.invokeLater(() -> {
//                RenderTask task = renderTask.getAndSet(null);
//                if (task == null) {
//                    return;
//                }
//                BufferedImage image = panel.createImage();
//                Graphics2D graphics = (Graphics2D) image.getGraphics();
//                render(graphics, task);
//                graphics.dispose();
//                panel.setImage(image);
//            });
//        });
//        loop.run();
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
        }

        long frameSecond = Math.floorDiv(System.nanoTime(), 1_000_000_000L);
        runningFrameCount++;
        if (runningFrameSecond != frameSecond) {
            frameCount = runningFrameCount;
            runningFrameCount = 0;
            runningFrameSecond = frameSecond;
        }
        if (debug) {
            graphics.drawString("fps: " + frameCount, 20, 20 + renderTask.lines().size() * graphics.getFontMetrics().getHeight());
        }
    }
}
