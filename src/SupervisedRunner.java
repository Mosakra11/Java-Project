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

                // a) Print the error message
                System.err.println("[" + workerName + "] Exception: " + e.getMessage());

                // b) Sleep for the current backoff duration
                try {
                    Thread.sleep(backoffMs);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    break;
                }

                // c) Update backoff: reset if ran 10+ seconds, otherwise double
                if (elapsedMs >= SUCCESS_DURATION_MS) {
                    backoffMs = INITIAL_BACKOFF_MS;
                } else {
                    backoffMs = Math.min(backoffMs * 2, MAX_BACKOFF_MS);
                }

                // Track restart in rolling window
                long now = System.currentTimeMillis();
                failureTimestamps.addLast(now);

                // Remove timestamps older than 30 seconds
                long cutoffTime = now - RESTART_WINDOW_MS;
                while (!failureTimestamps.isEmpty() && failureTimestamps.getFirst() < cutoffTime) {
                    failureTimestamps.removeFirst();
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

