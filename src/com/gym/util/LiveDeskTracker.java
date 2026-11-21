/**
 * Date: 2026-11-18 - Yash
 * Syllabus: Unit 3 (Lectures 9, 10, 11, 12, 13, 14) - 
 *           Thread & Runnable, start(), run(), sleep(), interrupt(),
 *           Thread Lifecycle, Daemon threads, and Synchronization
 */
package com.gym.util;

import javax.swing.*;
import java.text.SimpleDateFormat;
import java.util.Date;

public class LiveDeskTracker implements Runnable {

    private final JLabel clockLabel;
    private final JLabel statusLabel;
    private volatile boolean running = true;
    private Thread workerThread;
    private final SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm:ss a");

    public LiveDeskTracker(JLabel clockLabel, JLabel statusLabel) {
        this.clockLabel = clockLabel;
        this.statusLabel = statusLabel;
    }

    /**
     * Starts the monitor daemon thread (Lectures 9-12)
     */
    public synchronized void start() {
        if (workerThread == null || !workerThread.isAlive()) {
            running = true;
            workerThread = new Thread(this, "Desk-LiveMonitor-Thread");
            workerThread.setDaemon(true); // Daemon thread (Lecture 11)
            workerThread.start();
        }
    }

    /**
     * Stops the thread using interrupt (Lectures 9-10)
     */
    public synchronized void stop() {
        running = false;
        if (workerThread != null) {
            workerThread.interrupt();
        }
    }

    @Override
    public void run() {
        // Thread Lifecycle: RUNNABLE state (Lecture 11)
        while (running && !Thread.currentThread().isInterrupted()) {
            try {
                String currentTime = timeFormat.format(new Date());

                // Safely update Swing GUI components from background thread
                SwingUtilities.invokeLater(() -> {
                    if (clockLabel != null) {
                        clockLabel.setText(currentTime);
                    }
                    if (statusLabel != null) {
                        statusLabel.setText("ACTIVE");
                    }
                });

                // TIMED_WAITING state: sleep for 1 second (Lecture 10, 11)
                Thread.sleep(1000);

            } catch (InterruptedException e) {
                // Interruption handling (Lecture 10)
                Thread.currentThread().interrupt();
                break;
            }
        }
        // Thread Lifecycle: TERMINATED state (Lecture 11)
    }
}
