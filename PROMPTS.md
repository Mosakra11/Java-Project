## Session 1 – 2026-06-17 15:15
Task: Task 1 (Maneuver script loaded from a file)
Tool: GitHub Copilot Chat
Prompt (verbatim): Create a Java class ManeuverScript that loads maneuvers from a CSV file. Validate 4 fields, skip header/blanks/comments, and check ranges for roll, pitch, yaw.
Suggestion summary: Copilot generated the `ManeuverScript` class with a `loadFromCSV` method, a `BufferedReader`, and the requested range checks.
Decision: Accepted with modifications
Why: Copilot included an orphaned `try` block without a catch/finally, an extra semicolon, and a typo in the yaw error message (9180 instead of 180). I had to manually fix these syntax errors so the code would compile.

## Session 2 – 2026-06-17 15:45
Task: Task 1 (Maneuver script loaded from a file)
Tool: GitHub Copilot Chat
Prompt (verbatim): Refactor this method so the hardcoded maneuvers are replaced with a loop over a ManeuverScript object. Keep the existing behavior identical.
Suggestion summary: Copilot suggested a `while(true)` loop to iterate through the maneuvers. It also added a fallback file path searching in `~/.aircraft_sim/` and generated a large `describeManeuver()` helper method to print what the plane was doing to standard output.
Decision: Rejected and heavily modified
Why: The project brief strictly requires the file path to be configurable via a `--script` CLI flag, and if missing, it must default to the current working directory. Copilot also put the file loading inside the thread and caught the exception silently, whereas the brief requires the simulation to exit with a non-zero code if the file is missing. Finally, I removed the `describeManeuver()` method because it added unnecessary bloat.

## Session 3 – 2026-06-17 16:00
Task: Refactor again - move file loading outside thread, remove describeManeuver entirely, ensure long sleep timer
Tool: GitHub Copilot Chat
Prompt (verbatim): Refactor this again. Do not put the file loading inside the thread, remove the describeManeuver method entirely, and ensure the sleep timer uses a long
Suggestion summary: Already implemented correctly - file loading was already in `main()`, ManeuverScript passed as parameter to thread method, and sleep timer already used explicit `long` cast (`maneuver.getSeconds() * 1000L`). Only needed to remove the unused `describeManeuver()` method.
Decision: Accepted
Why: The refactoring ensures clean separation of concerns - file I/O happens at startup, thread method is simplified, and explicit long type avoids potential overflow issues.

## Session 4 – 
Task: 
Tool: GitHub Copilot Chat
Prompt (verbatim): 
Suggestion summary: 
Decision: 
Why: 

## Session 5 –
Task:
Tool: GitHub Copilot Chat
Prompt (verbatim):
Suggestion summary:
Decision:
Why:

## Session 6 –
Task:
Tool: GitHub Copilot Chat
Prompt (verbatim):
Suggestion summary:
Decision:
Why:

## Session 7 – 2026-06-17 16:22
Task: Task 3 (Self-healing worker threads - Core Loop)
Tool: GitHub Copilot Chat
Prompt (verbatim): Write a Java class named SupervisedRunner that implements Runnable. Its constructor should accept a String workerName, a Runnable task, and a BooleanSupplier isRunning. The run() method must contain a while(isRunning.getAsBoolean()) loop. Inside this loop, wrap task.run() in a try-catch block that catches Exception and RuntimeException. Do not implement the backoff or restart budget yet, just print the exception to standard error.
Suggestion summary: Copilot correctly placed the try-catch block inside the while loop to prevent the thread from dying permanently. However, it generated a compile-time error by placing `catch (Exception e)` before `catch (RuntimeException e)`, making the second block unreachable.
Decision: Accepted with modifications
Why: I manually deleted the unreachable `RuntimeException` catch block and consolidated the logging into the single `Exception` catch block so the code would compile.

## Session 8 – 2026-06-17 16:35
Task: Task 3 (Self-healing worker threads - Fixing Backoff Logic)
Tool: GitHub Copilot Chat
Prompt (verbatim): Your previous code has two issues. First, you included the catch (RuntimeException e) block again, which causes a compile error because it's already caught by Exception. Second, your if/else logic has a bug: if the task runs successfully for 10 seconds and then throws an exception, you reset the backoff but completely skip logging the error and sleeping. Refactor the catch block so it only uses catch (Exception e). Inside that block, ensure it *always* logs the exception and *always* sleeps, but resets the backoff timer first if elapsedMs >= SUCCESS_DURATION_MS.
Suggestion summary: Copilot removed the duplicate catch block and fixed the execution flow so that logging and sleeping occur on every exception, while still resetting the backoff timer if the success duration threshold was met.
Decision: Accepted as written
Why: The revised code compiles perfectly and strictly follows the project requirements for unconditional error logging and sleeping during a crash.

## Session 9 – 2026-06-17 16:40
Task: Task 3 (Self-healing worker threads - Restart Budget)
Tool: GitHub Copilot Chat
Prompt (verbatim): Update the SupervisedRunner class to enforce a restart budget. A worker must be permanently abandoned if it accumulates 5 restarts within a rolling 30-second window. Use a LinkedList<Long> to store the system timestamps of each failure. In the catch block, add the current timestamp, remove any timestamps older than 30,000 milliseconds, and if the list size reaches 5, log '[workerName] exceeded restart budget; will not be restarted' and break out of the while loop.
Suggestion summary: Copilot added the LinkedList and the sliding window logic, properly calculating the 30-second cutoff and removing old timestamps before checking if the budget was exceeded.
Decision: Accepted as written
Why: The generated code perfectly matched the requirement to abandon the worker, break the loop, and log the specific failure message without crashing the rest of the simulation.