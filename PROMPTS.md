# ManeuverScript Class

## Overview
The `ManeuverScript` class reads CSV files containing aircraft maneuver data and stores them as a `List<Maneuver>`.

## CSV Format
```
seconds,roll,pitch,yaw
8,0,0,0
12,2,0,2
```

## Features
- **Skip header** - Automatically skips the header line (seconds,roll,pitch,yaw)
- **Ignore blank lines** - Blank lines are skipped during parsing
- **Ignore comments** - Lines starting with `#` are ignored
- **Validate 4 fields** - Each data line must have exactly 4 comma-separated values
- **Store as List<Maneuver>** - Data is parsed and stored as a list of Maneuver objects

## Constructor
```java
public ManeuverScript(String filename) throws IOException
```
Creates a new ManeuverScript instance and loads maneuvers from the specified CSV file.

## Methods

### getManeuvers()
```java
public List<Maneuver> getManeuvers()
```
Returns a copy of the list of loaded maneuvers.

### size()
```java
public int size()
```
Returns the number of loaded maneuvers.

### getManeuver(int index)
```java
public Maneuver getManeuver(int index) throws IndexOutOfBoundsException
```
Gets a specific maneuver by index.

## Usage Example
```java
try {
    ManeuverScript script = new ManeuverScript("maneuvers.csv");
    List<Maneuver> maneuvers = script.getManeuvers();
    
    for (Maneuver m : maneuvers) {
        System.out.println("Seconds: " + m.getSeconds() + 
                         ", Roll: " + m.getRoll() + 
                         ", Pitch: " + m.getPitch() + 
                         ", Yaw: " + m.getYaw());
    }
} catch (IOException e) {
    System.err.println("Error reading file: " + e.getMessage());
} catch (IllegalArgumentException e) {
    System.err.println("Invalid CSV format: " + e.getMessage());
}
```

## Error Handling
The class provides detailed error messages including:
- Line numbers where errors occurred
- Expected vs. actual field counts
- Number format errors with context

## Implementation Details
The class uses `BufferedReader` to efficiently read the CSV file line by line:
- First non-blank, non-comment line is treated as the header
- Remaining lines are parsed as maneuver data
- Each field is trimmed of whitespace before parsing
- Integers are parsed for the seconds field
- Doubles are parsed for roll, pitch, and yaw fields
