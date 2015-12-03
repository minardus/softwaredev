public class Settings {

    public static final int Port = 61000;
    // time to stay on orange on traffic lights for cars
    public static final int OrangeTime = 3 * 1000;
    // time to stay maximal red when trafficlight occupied = true
    public static final int MaxRedTime = 120 * 1000;
    // from this number on, the light has no priority (still has a maximum of 120secs to go green though)
    public static int noPriorityThreshold = 17;

}
