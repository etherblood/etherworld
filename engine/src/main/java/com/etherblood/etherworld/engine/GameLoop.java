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
                // TODO: calculate sleep duration or find better solution
                Thread.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace(System.err);
            }
        }
    }

    public void stop() {
        active.set(false);
    }
}
