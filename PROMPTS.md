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
