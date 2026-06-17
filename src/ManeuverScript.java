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
                int seconds;
                double roll;
                double pitch;
                double yaw;

                try {
                    seconds = Integer.parseInt(fields[0].trim());
                } catch (NumberFormatException e) {
                    throw new IllegalArgumentException(
                            "Script error on line " + lineNumber + " field 1 (seconds): \"" + fields[0] + "\" is not a number"
                    );
                }
                try {
                    roll = Double.parseDouble(fields[1].trim());
                } catch (NumberFormatException e) {
                    throw new IllegalArgumentException(
                            "Script error on line " + lineNumber + " field 2 (roll): \"" + fields[1] + "\" is not a number"
                    );
                }
                try {
                    pitch = Double.parseDouble(fields[2].trim());
                } catch (NumberFormatException e) {
                    throw new IllegalArgumentException(
                            "Script error on line " + lineNumber + " field 3 (pitch): \"" + fields[2] + "\" is not a number"
                    );
                }
                try {
                    yaw = Double.parseDouble(fields[3].trim());
                } catch (NumberFormatException e) {
                    throw new IllegalArgumentException(
                            "Script error on line " + lineNumber + " field 4 (yaw): \"" + fields[3] + "\" is not a number"
                    );
                }


                if (roll < -180 || roll > 180) {
                    throw new IllegalArgumentException(
                            "Script error on line " + lineNumber + ": roll out of range (-180 to 180)"
                    );
                }
                if (pitch < -90 || pitch > 90) {
                    throw new IllegalArgumentException(
                            "Script error on line " + lineNumber + ": pitch out of range (-90 to 90)"
                    );
                }
                if (yaw < -180 || yaw > 180) {
                    throw new IllegalArgumentException(
                            "Script error on line " + lineNumber + ": yaw out of range (-180 to 180)"
                    );
                }
                // Create and add maneuver
                maneuvers.add(new Maneuver(seconds, roll, pitch, yaw));
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

