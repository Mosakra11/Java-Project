import java.io.*;
import java.util.*;

public class ManeuverScript {
    private List<Maneuver> maneuvers;

    /**
     * Creates a new ManeuverScript and loads maneuvers from a CSV file.
     *
     * @param filename the path to the CSV file
     * @throws IOException if the file cannot be read
     * @throws IllegalArgumentException if the CSV format is invalid
     */
    public ManeuverScript(String filename) throws IOException {
        this.maneuvers = new ArrayList<>();
        loadFromCSV(filename);
    }

    /**
     * Loads maneuvers from a CSV file.
     * Expected format: seconds,roll,pitch,yaw
     * - Skips header line
     * - Ignores blank lines
     * - Ignores lines starting with #
     * - Validates that each line has exactly 4 fields
     *
     * @param filename the path to the CSV file
     * @throws IOException if the file cannot be read
     * @throws IllegalArgumentException if a line has invalid format
     */
     private void loadFromCSV(String filename) throws IOException {
         try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
             String line;
             int lineNumber = 0;
             boolean headerSkipped = false;
             // Collect script errors across the whole file so we can report all problems at once
             List<String> scriptErrors = new ArrayList<>();

             while ((line = reader.readLine()) != null) {
                 lineNumber++;

                 // Skip header (first non-blank, non-comment line)
                 if (!headerSkipped) {
                     if (line.trim().isEmpty() || line.trim().startsWith("#")) {
                         continue;
                     }
                     if (line.trim().equalsIgnoreCase("seconds,roll,pitch,yaw")) {
                         headerSkipped = true;
                         continue;
                     }
                     // If first non-blank line isn't the header, treat it as data
                     headerSkipped = true;
                 }

                 // Ignore blank lines
                 if (line.trim().isEmpty()) {
                     continue;
                 }

                 // Ignore comment lines
                 if (line.trim().startsWith("#")) {
                     continue;
                 }

                 // Parse the line

                 String[] fields = line.split(",");

                 // Validate 4 fields
                 if (fields.length != 4) {
                     throw new IllegalArgumentException(
                             "Script error on line " + lineNumber +
                                     ": expected 4 fields but found " + fields.length
                     );
                 }

                 // Collect errors per line instead of throwing immediately so we can report all script issues
                 List<String> lineErrors = new ArrayList<>();

                 Integer seconds = null;
                 Double roll = null;
                 Double pitch = null;
                 Double yaw = null;

                 // Parse each field, recording parse errors
                 try {
                     seconds = Integer.parseInt(fields[0].trim());
                 } catch (NumberFormatException e) {
                     lineErrors.add("field 1 (seconds): \"" + fields[0] + "\" is not a number");
                 }
                 try {
                     roll = Double.parseDouble(fields[1].trim());
                 } catch (NumberFormatException e) {
                     lineErrors.add("field 2 (roll): \"" + fields[1] + "\" is not a number");
                 }
                 try {
                     pitch = Double.parseDouble(fields[2].trim());
                 } catch (NumberFormatException e) {
                     lineErrors.add("field 3 (pitch): \"" + fields[2] + "\" is not a number");
                 }
                 try {
                     yaw = Double.parseDouble(fields[3].trim());
                 } catch (NumberFormatException e) {
                     lineErrors.add("field 4 (yaw): \"" + fields[3] + "\" is not a number");
                 }

                 // Range checks only if parsing succeeded
                 if (roll != null) {
                     if (roll < -180 || roll > 180) {
                         lineErrors.add("roll out of range (-180 to 180)");
                     }
                 }
                 if (pitch != null) {
                     if (pitch < -90 || pitch > 90) {
                         lineErrors.add("pitch out of range (-90 to 90)");
                     }
                 }
                 if (yaw != null) {
                     if (yaw < -180 || yaw > 180) {
                         lineErrors.add("yaw out of range (-180 to 180)");
                     }
                 }

                 // If there were any errors for this line, collect them into a single message and continue parsing
                 if (!lineErrors.isEmpty()) {
                     StringBuilder sb = new StringBuilder();
                     sb.append("Script error on line ").append(lineNumber).append(": ");
                     for (int i = 0; i < lineErrors.size(); i++) {
                         if (i > 0) sb.append("; ");
                         sb.append(lineErrors.get(i));
                     }
                     scriptErrors.add(sb.toString());
                     // Skip adding this maneuver because the line had errors
                     continue;
                 }

                 // Create and add maneuver
                 maneuvers.add(new Maneuver(seconds, roll, pitch, yaw));
             }
             // After reading all lines, if we collected any script errors, throw a combined exception
             if (!scriptErrors.isEmpty()) {
                 throw new IllegalArgumentException(String.join("\n", scriptErrors));
             }
         }
     }

    /**
     * Gets the list of loaded maneuvers.
     *
     * @return a list of Maneuver objects
     */
    public List<Maneuver> getManeuvers() {
        return new ArrayList<>(maneuvers);
    }

    /**
     * Gets the number of loaded maneuvers.
     *
     * @return the count of maneuvers
     */
    public int size() {
        return maneuvers.size();
    }

    /**
     * Gets a specific maneuver by index.
     *
     * @param index the index of the maneuver
     * @return the Maneuver at the specified index
     * @throws IndexOutOfBoundsException if index is out of range
     */
    public Maneuver getManeuver(int index) {
        return maneuvers.get(index);
    }
}

