public class Maneuver {
    private int seconds;
    private double roll;
    private double pitch;
    private double yaw;

    public Maneuver(int seconds, double roll, double pitch, double yaw){
        this.seconds = seconds;
        this.roll = roll;
        this.pitch = pitch;
        this.yaw = yaw;
    }

    public int getSeconds() {return seconds;}
    public double getRoll() {return roll;}
    public double getPitch() {return pitch;}
    public double getYaw() {return yaw;}
}
