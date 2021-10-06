package com.etherblood.etherworld.engine;

import java.util.concurrent.atomic.AtomicBoolean;

public class GameLoop implements Runnable {
    private final AtomicBoolean active = new AtomicBoolean(false);
    private final int fps;
    private final Runnable updater;

    public GameLoop(int fps, Runnable updater) {
        this.fps = fps;
        this.updater = updater;
    }

    @SuppressWarnings("BusyWait")
    @Override
    public void run() {
        active.set(true);
        long startNanos = System.nanoTime();
        long frames = 0;
        while (active.get()) {
            while (1_000_000_000 * frames <= (System.nanoTime() - startNanos) * fps) {
                updater.run();
                frames++;
            }
            try {
                long sleepNanos = startNanos - System.nanoTime() + 1_000_000_000 * frames / fps;
                if (sleepNanos <= 1_000_000) {
                    Thread.sleep(1);
                } else {
                    Thread.sleep(Math.floorDiv(sleepNanos, 1_000_000L), Math.floorMod(sleepNanos, 1_000_000));
                }
            } catch (InterruptedException e) {
                e.printStackTrace(System.err);
            }
        }
    }

    public void stop() {
        active.set(false);
    }
}
