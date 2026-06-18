import java.util.*;
import java.util.function.BooleanSupplier;

/**
 * A Runnable wrapper that supervises task execution with exception handling.
 * Executes a task repeatedly while a running condition is true.
 * Enforces exponential backoff and a restart budget.
 */
public class SupervisedRunner implements Runnable {
    private final String workerName;
    private final Runnable task;
    private final BooleanSupplier isRunning;
    private static final long INITIAL_BACKOFF_MS = 100;
    private static final long MAX_BACKOFF_MS = 5000;
    private static final long SUCCESS_DURATION_MS = 10000;
    private static final int MAX_RESTARTS = 5;
    private static final long RESTART_WINDOW_MS = 30000;
    private final LinkedList<Long> failureTimestamps;

    /**
     * Creates a new SupervisedRunner.
     *
     * @param workerName the name of the worker (for logging)
     * @param task the Runnable task to execute
     * @param isRunning a BooleanSupplier that determines if the loop should continue
     */
    public SupervisedRunner(String workerName, Runnable task, BooleanSupplier isRunning) {
        this.workerName = workerName;
        this.task = task;
        this.isRunning = isRunning;
        this.failureTimestamps = new LinkedList<>();
    }

    @Override
    public void run() {
        long backoffMs = INITIAL_BACKOFF_MS;

        while (isRunning.getAsBoolean()) {
            long startTime = System.currentTimeMillis();
            try {
                task.run();
            } catch (Exception e) {
                long elapsedMs = System.currentTimeMillis() - startTime;

                // a) Print the error message and stack trace for clarity
                System.err.println("[" + workerName + "] Exception caught: " + e.toString());
                e.printStackTrace(System.err);

                // Track restart in rolling window (timestamp of this failure)
                long now = System.currentTimeMillis();
                failureTimestamps.addLast(now);

                // Remove timestamps older than the restart window
                long cutoffTime = now - RESTART_WINDOW_MS;
                while (!failureTimestamps.isEmpty() && failureTimestamps.getFirst() < cutoffTime) {
                    failureTimestamps.removeFirst();
                }

                int recentFailures = failureTimestamps.size();

                // Log the planned backoff and restart count
                System.err.println("[" + workerName + "] will restart after " + backoffMs + " ms (" + recentFailures + " failures in last " + (RESTART_WINDOW_MS/1000) + "s)");

                // Sleep for the current backoff duration
                try {
                    Thread.sleep(backoffMs);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    break;
                }

                // c) Update backoff: reset if ran SUCCESS_DURATION_MS+ ms, otherwise double
                if (elapsedMs >= SUCCESS_DURATION_MS) {
                    System.err.println("[" + workerName + "] run was stable (" + elapsedMs + " ms); resetting backoff to " + INITIAL_BACKOFF_MS + " ms");
                    backoffMs = INITIAL_BACKOFF_MS;
                } else {
                    long newBackoff = Math.min(backoffMs * 2, MAX_BACKOFF_MS);
                    System.err.println("[" + workerName + "] increasing backoff: " + backoffMs + " -> " + newBackoff + " ms");
                    backoffMs = newBackoff;
                }

                // Check if restart budget exceeded
                if (failureTimestamps.size() >= MAX_RESTARTS) {
                    System.err.println("[" + workerName + "] exceeded restart budget; will not be restarted");
                    break;
                }
            }
        }
    }
}

