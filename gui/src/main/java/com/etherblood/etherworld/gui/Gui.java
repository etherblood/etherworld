package com.etherblood.etherworld.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;

public class Gui {
    private final AtomicReference<RenderTask> renderTask = new AtomicReference<>();
    private final Set<Integer> pressedKeys = Collections.synchronizedSet(new HashSet<>());
    private boolean debug = false;
    private long runningFrameSecond;
    private int runningFrameCount;
    private int frameCount;

    private PictureBox panel;

    public void start() {
        int windowWidth = 1600;
        int windowHeight = 800;

        JFrame jFrame = new JFrame("Gaem?");
        jFrame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        jFrame.setLayout(new BorderLayout());
        panel = new PictureBox();
        panel.setBackground(Color.DARK_GRAY);
        panel.setPreferredSize(new Dimension(windowWidth, windowHeight));
        panel.setVisible(true);
        jFrame.setSize(windowWidth, windowHeight);
        jFrame.add(panel, BorderLayout.CENTER);

        jFrame.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                pressedKeys.add(e.getKeyCode());
                switch (e.getKeyCode()) {
                    case KeyEvent.VK_F1 -> debug = !debug;
                    case KeyEvent.VK_ESCAPE -> System.exit(0);
                }
            }

            @Override
            public void keyReleased(KeyEvent e) {
                pressedKeys.remove(e.getKeyCode());
            }
        });

        jFrame.pack();
        jFrame.setVisible(true);
    }

    public void render(RenderTask task) {
        renderTask.set(task);
        SwingUtilities.invokeLater(this::update);
    }

    private void update() {
        RenderTask task = renderTask.getAndSet(null);
        if (task == null) {
            return;
        }
        BufferedImage image = panel.createImage();
        Graphics2D graphics = (Graphics2D) image.getGraphics();
        render(graphics, task);
        graphics.dispose();
        panel.setImage(image);
    }

    private void render(Graphics2D graphics, RenderTask renderTask) {
        AffineTransform transform = AffineTransform.getScaleInstance(2, 2);
        transform.translate(-renderTask.camera().x(), -renderTask.camera().y());
        graphics.setTransform(transform);
        graphics.setBackground(renderTask.background());
        graphics.clearRect(
                renderTask.camera().x(),
                renderTask.camera().y(),
                renderTask.camera().width(),
                renderTask.camera().height());
        graphics.setColor(Color.WHITE);
        if (debug) {
            for (RenderChunk chunk : renderTask.chunks()) {
                renderImage(graphics, chunk.sheet(), chunk.destination(), chunk.hitbox());
            }
        }
        if (!debug) {
            for (RenderChunk chunk : renderTask.chunks()) {
                renderImage(graphics, chunk.sheet(), chunk.destination(), chunk.background());
            }
        }
        for (RenderSprite sprite : renderTask.sprites()) {
            renderImage(graphics, sprite.sheet(), sprite.destination(), sprite.source());
        }
        if (!debug) {
            for (RenderChunk chunk : renderTask.chunks()) {
                renderImage(graphics, chunk.sheet(), chunk.destination(), chunk.foreground());
            }
        }
        if (debug) {
            for (DebugRectangle rectangle : renderTask.rectangles()) {
                if (rectangle.fill()) {
                    graphics.setColor(rectangle.color());
                    RenderRectangle destination = normalize(rectangle.destination());
                    graphics.fillRect(
                            destination.x(),
                            destination.y(),
                            destination.width(),
                            destination.height());
                }
            }
            for (DebugRectangle rectangle : renderTask.rectangles()) {
                if (!rectangle.fill()) {
                    graphics.setColor(rectangle.color());
                    RenderRectangle destination = normalize(rectangle.destination());
                    graphics.drawRect(
                            destination.x(),
                            destination.y(),
                            destination.width(),
                            destination.height());
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

    private RenderRectangle normalize(RenderRectangle rectangle) {
        return new RenderRectangle(
                Math.min(rectangle.aX(), rectangle.bX()),
                Math.min(rectangle.aY(), rectangle.bY()),
                Math.abs(rectangle.width()),
                Math.abs(rectangle.height())
        );
    }

    private void renderImage(Graphics2D graphics, Image image, RenderRectangle destination, RenderRectangle source) {
        graphics.drawImage(image,
                destination.aX(),
                destination.aY(),
                destination.bX(),
                destination.bY(),
                source.aX(),
                source.aY(),
                source.bX(),
                source.bY(),
                null);
    }

    public Set<Integer> getPressedKeys() {
        return Set.copyOf(pressedKeys);
    }
}
