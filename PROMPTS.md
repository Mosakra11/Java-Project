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
Decision: Implemented as specified
Why: The refactoring ensures clean separation of concerns - file I/O happens at startup, thread method is simplified, and explicit long type avoids potential overflow issues.
