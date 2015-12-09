import org.json.simple.JSONObject;

import java.util.Set;

public class TrafficLight {

    public enum Status {
        RED,
        ORANGE,
        GREEN,
        BUS_STRAIGHT_AND_RIGHT,
        BUS_STRAIGHT,
        BUS_RIGHT
    }

    private final int fakeId;   // the id to be used to check if state is possible
    private final int parent; // if >= 0, then this trafficlight is linked to another traffic lights
    private final int id;
    private final boolean priority;
    private int status;
    private boolean occupied = false;
    private boolean fakeOccupied = false;
    private int secsOnRed = 0;

    private volatile MaxRedTimerThread maxRedTimerThread = null;

    public TrafficLight(int id, boolean priority, int fakeId)
    {
        this(id, -1, priority, fakeId);
    }

    public TrafficLight(int id, int parent, boolean priority)
    {
        this(id, parent, priority, -1);
    }

    public TrafficLight(int id, int parent, boolean priority, int fakeId)
    {
        this.fakeId = fakeId;
        this.parent = parent;
        this.id = id;
        this.priority = priority;
        SetStatus(Status.RED);
        System.out.println("id: " + this.id + "\tparent: " + this.parent + "\tfakeId: " + this.fakeId);
    }

    public final int GetFakeID()
    {
        return fakeId;
    }

    public final int GetParent()
    {
        return this.parent;
    }

    public final int GetID()
    {
        return this.id;
    }

    public final boolean GetPriority()
    {
        return this.priority;
    }

    public int GetStatus()
    {
        return this.status;
    }

    public synchronized void SetStatus(Status status)
    {
        this.status = status.ordinal();
    }

    public void SetFakeOccupied(boolean fakeOccupied)
    {
        this.fakeOccupied = fakeOccupied;
    }

    public boolean GetFakeOccupied()
    {
        return fakeOccupied;
    }

    public boolean GetOccupied()
    {
        return occupied;
    }

    public synchronized void SetOccupied(boolean occupied)
    {
        this.occupied = occupied;
        if(this.occupied)
        {
            RunMaxRedTimerThread();
        }
        else
        {
            if(maxRedTimerThread != null)
                DestroyMaxRedTimerThread();
        }
    }

    public JSONObject getJSONCompat() {
        return (new JSONStructs.Stoplichten(GetID(), GetStatus())).toJSONObject();
    }

    public synchronized int GetSecsOnRed()
    {
        return secsOnRed;
    }

    private synchronized void SetSecsOnRed(int secsOnRed)
    {
        this.secsOnRed = secsOnRed;
    }

    /**
     * This gets called if SetOccupied(true)
     * Start a timer which holds how long a trafficlight is on red already
     * (on red when is occupied that is)
     */
    private void RunMaxRedTimerThread()
    {
        if(maxRedTimerThread == null)
        {
            maxRedTimerThread = new MaxRedTimerThread();
            maxRedTimerThread.start();
        }
    }

    /**
     * This gets called if SetOcuppied(false)
     * Set trafficlight on Orange and 3 seconds later on Red
     * Also stop maxRedTimerThread (which is not fully used on this moment, just for
     * prioritizing trafficlights
     */
    private void DestroyMaxRedTimerThread()
    {
        if(maxRedTimerThread != null) {
            //maxRedTimerThread.interrupt();
            maxRedTimerThread = null;
            new Thread() {
                public void run() {
                    SetSecsOnRed(0);
                    SetStatus(Status.ORANGE);
                    Intersection.ServerCallback();  // push orange
                    try {
                        sleep(parent > 17 || id > 17 ? Settings.OrangeTime * 4 : Settings.OrangeTime);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    SetStatus(Status.RED);
                    Intersection.ServerCallback(); // push red
                }
            }.start();
        }
    }

    private String GetNamedStatus()
    {
        switch(GetStatus())
        {
            case 0:
                return "RED";
            case 1:
                return "ORANGE";
            case 2:
                return "GREEN";
            case 3:
                return "BUS_STRAIGHT_AND_RIGHT";
            case 4:
                return "BUS_STRAIGHT";
            case 5:
                return "BUS_RIGHT";
        }
        return "ERROR";
    }

    private class MaxRedTimerThread extends Thread
    {

        @Override
        public void run()
        {
            while(true)
            {
                try {
                    System.out.println("ping from " + GetID() + "\t" + GetSecsOnRed() + " secs on " + GetNamedStatus());
                    int secsOnRed = GetSecsOnRed();
                    sleep(1000);
                    if(maxRedTimerThread == null) {
                        break;
                    }
                    else if(secsOnRed < Settings.MaxRedTime)
                    {
                        SetSecsOnRed(secsOnRed + 1);
                    }
                    else
                        break;
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

    }

}
