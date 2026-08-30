package com.devcli.ui;

public class LoadingSpinner implements AutoCloseable {

    private static final String[] SPINNER_FRAMES = {"⠋", "⠙", "⠹", "⠸", "⠼", "⠴", "⠦", "⠧", "⠇", "⠏"};
    
    private final String taskName;
    private volatile boolean running = true;
    private Thread thread;

    public LoadingSpinner(String taskName) {
        this.taskName = taskName;
    }

    public static LoadingSpinner start(String taskName) {
        LoadingSpinner spinner = new LoadingSpinner(taskName);
        spinner.runAsync();
        return spinner;
    }

    private void runAsync() {
        thread = new Thread(() -> {
            int frameIdx = 0;
            String paddedTask = String.format("%-34s", taskName);
            while (running) {
                String frame = SPINNER_FRAMES[frameIdx % SPINNER_FRAMES.length];
                System.out.print("\r  " + paddedTask + " " + AnsiStyle.boldCyan(frame));
                System.out.flush();
                frameIdx++;
                try {
                    Thread.sleep(80);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        });
        thread.setDaemon(true);
        thread.start();
    }

    public void stopSuccess(String message) {
        running = false;
        if (thread != null) {
            try { thread.join(200); } catch (InterruptedException ignored) {}
        }
        String label = message != null ? message : taskName;
        String paddedTask = String.format("%-34s", label);
        System.out.print("\r  " + paddedTask + " " + AnsiStyle.boldGreen("✓") + "    \n");
        System.out.flush();
    }

    public void stopError(String message) {
        running = false;
        if (thread != null) {
            try { thread.join(200); } catch (InterruptedException ignored) {}
        }
        String label = message != null ? message : taskName;
        String paddedTask = String.format("%-34s", label);
        System.out.print("\r  " + paddedTask + " " + AnsiStyle.boldRed("✗") + "    \n");
        System.out.flush();
    }

    @Override
    public void close() {
        if (running) {
            stopSuccess(taskName);
        }
    }
}
