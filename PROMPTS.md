## Session 1 – 2026-06-17 15:15
Task: Task 1 (Maneuver script loaded from a file)
Tool: GitHub Copilot Chat
Prompt (verbatim): Create a Java class ManeuverScript that loads maneuvers from a CSV file. Validate 4 fields, skip header/blanks/comments, and check ranges for roll, pitch, yaw.
Suggestion summary: Copilot generated the `ManeuverScript` class and a `loadFromCSV` method with the right range checks.
Decision: Accepted with modifications
Why: Copilot gave me a weird orphaned `try` block that didn't have a catch or finally, and it made a typo in the yaw error message (typed 9180 instead of 180). I just fixed the syntax errors manually so it would actually compile.

## Session 2 – 2026-06-17 15:45
Task: Task 1 (Maneuver script loaded from a file)
Tool: GitHub Copilot Chat
Prompt (verbatim): Refactor this method so the hardcoded maneuvers are replaced with a loop over a ManeuverScript object. Keep the existing behavior identical.
Suggestion summary: Copilot suggested a `while(true)` loop to go through the maneuvers, but it also tried to do way too much. It added fallback file paths and wrote a massive helper method to print out what the plane was doing.
Decision: Rejected and heavily modified
Why: The project rubric says we have to use the `--script` flag, and if the file is missing, the program should crash out. Copilot tried to catch the missing file silently inside the thread, which is wrong. I had to rip out a lot of its extra code to keep it simple.

## Session 3 – 2026-06-17 16:00
Task: Refactor again - move file loading outside thread, remove describeManeuver entirely, ensure long sleep timer
Tool: GitHub Copilot Chat
Prompt (verbatim): Refactor this again. Do not put the file loading inside the thread, remove the describeManeuver method entirely, and ensure the sleep timer uses a long
Suggestion summary: Copilot mostly just cleaned up the code I had already started tweaking. It removed the extra print methods.
Decision: Accepted
Why: Loading the file inside `main()` just makes way more sense. If the CSV is broken, I want the simulation to fail immediately on startup, rather than waiting for the thread to launch and then crashing.

## Session 4 – 2026-06-18 
Task: Task 2 (Replace polling with Observer pattern for altitude data)
Tool: GitHub Copilot Chat
Prompt (verbatim): Can you replace polling with the Observer pattern? Right now the code moves altitude data from the simulation to the GUI through polling.
Suggestion summary: Copilot generated an AltitudeObserver interface and suggested adding observer management methods to AircraftGUI (addAltitudeObserver, removeAltitudeObserver, notifyAltitudeChanged). It recommended updating updateAircraft() to notify observers instead of just storing the altitude value, and registering the panel as an observer in createAndShowGUI().
Decision: Accepted as written
Why: The refactoring eliminates unnecessary polling every frame (60 FPS) and replaces it with event-driven updates that only notify observers when altitude actually changes. This reduces CPU usage, decouples the GUI from the panel via an interface, and makes it easy to add new altitude observers in the future. The observer pattern is thread-safe with synchronized operations on the observer list.

## Session 5 – 2026-06-18
Task: Task 2 (Replace polling with Observer pattern for orientation data - roll, pitch, yaw)
Tool: GitHub Copilot Chat
Prompt (verbatim): Define a Java interface DirectionControlListener with a single method void onDirectionChanged(DirectionControl control). Give DirectionControl two methods addListener(DirectionControlListener) and removeListener(DirectionControlListener), backed by a thread-safe collection (CopyOnWriteArrayList). Every time update() changes the current value, iterate through the listener list and invoke the callback on each instance. Store the most recent value in a volatile double field per axis, and stop calling getCurrentValue() from inside the Swing timer. Modify AircraftGUI so that in its constructor it registers listeners with all three DirectionControl instances.
Suggestion summary: Implementation created DirectionControlListener interface, added CopyOnWriteArrayList listeners to DirectionControl, modified update() to notify listeners via volatile field, implemented DirectionControlListener in AircraftGUI with onDirectionChanged() callback, and removed polling of getCurrentValue() from updateAircraft() and Swing Timer.
Decision: Accepted as written
Why: This completes the transition from polling to event-driven updates for all flight orientation data. CopyOnWriteArrayList is the optimal choice (many reads/few writes). The volatile field ensures EDT visibility without synchronization. Push-based notifications fire on the simulation thread while EDT safely reads cached values, respecting Swing's threading model. Eliminates redundant polling every 33ms when values change far less frequently.

THREAD SAFETY - SAFE PUBLICATION GUARANTEE:
(1) Simulation thread calls update(), writes to volatile volatileCurrentValue (volatile write)
(2) This triggers onDirectionChanged() listener on same thread, which reads the volatile value and stores in roll/pitch/yaw fields
(3) EDT reads roll/pitch/yaw in Swing Timer with no synchronization needed
The Java Memory Model guarantees that the volatile write-in step 1 happens-before any subsequent operation. EDT always sees the most recent value because volatile operations bypass CPU caches and force memory coherency.

## Session 6 – 2026-06-18
Task: Task 2 (Document thread-safe publication guarantee in code comments)
Tool: GitHub Copilot Chat
Prompt (verbatim): Can you add a comment block (not too long) in code or in the prompts.md that explains in plain text English how the code guarantees safe publication of the value across threads?
Suggestion summary: Added detailed code comment in DirectionControl.java explaining the three-step safe publication process: (1) simulation thread volatile write, (2) listener callback reads volatile value, (3) EDT reads stored field. Explained that Java Memory Model guarantees volatile write happens-before any subsequent operation.
Decision: Accepted as written
Why: Critical for code maintainability and auditing. Developers reading this code need to understand WHY volatile is sufficient rather than using locks. The comment clearly shows the happens-before relationship that guarantees EDT always sees the most recent value because volatile operations bypass CPU caches and force memory coherency.

## Session 7 – 2026-06-17 16:42
Task: Task 3 (Self-healing worker threads - Core Loop)
Tool: GitHub Copilot Chat
Prompt (verbatim): Write a Java class named SupervisedRunner that implements Runnable. Its constructor should accept a String workerName, a Runnable task, and a BooleanSupplier isRunning. The run() method must contain a while(isRunning.getAsBoolean()) loop. Inside this loop, wrap task.run() in a try-catch block that catches Exception and RuntimeException. Do not implement the backoff or restart budget yet, just print the exception to standard error.
Suggestion summary: It put the try-catch in the right place so the thread wouldn't completely die. But it put `catch (Exception e)` before `catch (RuntimeException e)`, which causes a compile error.
Decision: Accepted with modifications
Why: You can't catch a specific exception after a general one in Java. I just deleted the unreachable RuntimeException block myself to get the file to compile.

## Session 8 – 2026-06-17 16:58
Task: Task 3 (Self-healing worker threads - Fixing Backoff Logic)
Tool: GitHub Copilot Chat
Prompt (verbatim): Your previous code has two issues. First, you included the catch (RuntimeException e) block again, which causes a compile error because it's already caught by Exception. Second, your if/else logic has a bug: if the task runs successfully for 10 seconds and then throws an exception, you reset the backoff but completely skip logging the error and sleeping. Refactor the catch block so it only uses catch (Exception e). Inside that block, ensure it *always* logs the exception and *always* sleeps, but resets the backoff timer first if elapsedMs >= SUCCESS_DURATION_MS.
Suggestion summary: Copilot fixed the duplicate catch block and rearranged the if/else statements so that logging and sleeping happen every single time it crashes.
Decision: Accepted as written
Why: The old logic was skipping the sleep timer entirely if the task had been running well for 10 seconds. This new version actually follows the rubric to unconditionally log and sleep during a crash.

## Session 9 – 2026-06-17 17:02
Task: Task 3 (Self-healing worker threads - Restart Budget)
Tool: GitHub Copilot Chat
Prompt (verbatim): Update the SupervisedRunner class to enforce a restart budget. A worker must be permanently abandoned if it accumulates 5 restarts within a rolling 30-second window. Use a LinkedList<Long> to store the system timestamps of each failure. In the catch block, add the current timestamp, remove any timestamps older than 30,000 milliseconds, and if the list size reaches 5, log '[workerName] exceeded restart budget; will not be restarted' and break out of the while loop.
Suggestion summary: Copilot correctly used the LinkedList to store the crash times. It added a while loop to check the math and drop any timestamps older than 30 seconds.
Decision: Accepted as written
Why: It did exactly what I asked. If the list hits 5 crashes, it breaks the loop and gives up on the thread without crashing the whole application.

## Session 10 – 2026-06-17 17:25
Task: Task 3 (Self-healing worker threads - Failure Injection Test)
Tool: GitHub Copilot Chat
Prompt (verbatim): Add a CLI flag –inject-failures that makes the turbulence thread throw a RuntimeException at the 3-, 6-, and 9-second marks. With the flag set, the simulation must keep running for at least 60 seconds after the burst. Your log should clearly show three exceptions caught, three restarts with increasing backoff, and the simulation recovering.
Suggestion summary: Copilot added the logic to check for `--inject-failures` in the arguments, and updated the turbulence thread to throw exceptions at those exact second marks using `System.currentTimeMillis()`.
Decision: Accepted with modifications
Why: I needed this flag to actually test if my supervisor worked for the demo. It set up the exceptions perfectly, but introduced a bug with the timer resetting that I had to fix in the next prompt.

## Session 11 – 2026-06-17 17:47
Task: Task 3 (Self-healing worker threads - Timer Reset Bug Fix)
Tool: GitHub Copilot Chat
Prompt (verbatim): My inject-failures test is stuck in an infinite loop. Every time my SupervisedRunner catches the exception and restarts the turbulence task, the long taskStartTime = System.currentTimeMillis(); line runs again and resets my timer to zero. I need the timer to only start exactly once when the simulation first boots up, so it actually hits the global 3, 6, and 9-second marks instead of crashing every 3 seconds forever. How do I fix my code so the timer doesn't reset?
Suggestion summary: Copilot told me the timer was resetting because the variable was declared inside the runnable task. It told me to declare the start time out in `main()` instead and pass it in.
Decision: Accepted as written
Why: I was stuck in a completely annoying infinite loop. By grabbing the system time just once when the app boots, the failures actually happen at the correct times and the thread can finally recover instead of crashing forever.

## Session 12 – 2026-06-17 18:30
Task: Task 3 (Fixing Failure Injection Logic)
Tool: GitHub Copilot Chat
Prompt (verbatim): Refactor the createTurbulenceTask method. I need to fix the failure injection logic. 1. Wrap the entire task logic inside a while(running.get()) loop. 2. The Thread.sleep(200) must be inside the loop, at the very end. 3. Ensure the failure injection uses System.currentTimeMillis() - simulationBootTime so it triggers at global timestamps (3s, 6s, 9s). 4. If an exception is thrown, it must not be caught inside this method. Let the exception bubble up so the SupervisedRunner can handle the restart.
Suggestion summary: Copilot suggested wrapping the task in a while loop so it keeps running instead of exiting immediately, and told me to let the exception bubble up so the supervisor can catch it.
Decision: Accepted as written
Why: My thread was finishing before it ever reached the 3-second mark, so the failure injection never triggered. Moving the loop inside keeps the thread running and lets it actually throw the exceptions so my supervisor can catch them.

## Session 13 – 2026-06-17 18:35
Task: Task 3 (Supervisor Pattern Implementation)
Tool: GitHub Copilot Chat
Prompt (verbatim): I need to refactor the run() method in SupervisedRunner. Here are the strict requirements:Keep the while(isRunning.getAsBoolean()) loop. Inside the loop, task.run() must be the very first thing called. Wrap task.run() in a single try-catch block that catches Exception. Inside the catch block:a) Print the error message using System.err.println. b) Sleep for the current backoff duration. c) If the task ran for more than 10 seconds before crashing, reset the backoff timer. Otherwise, double the backoff duration. do not add any other catch blocks. do not remove the restart logic.
Suggestion summary: Copilot generated a robust run() method that effectively manages task restarts, implements exponential backoff, and ensures task failures are captured without terminating the thread.
Decision: Accepted as written
Why: This ensures that our "Self-Healing" system remains functional even during repeated failures, directly satisfying the project requirement for resilient worker threads.