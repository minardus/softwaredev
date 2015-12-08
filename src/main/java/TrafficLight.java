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

    private final int parent; // if >= 0, then this trafficlight is linked to another traffic lights
    private final int id;
    private final boolean priority;
    private int status;
    private boolean occupied = false;
    private int secsOnRed = 0;

    private volatile MaxRedTimerThread maxRedTimerThread = null;

    public TrafficLight(int id, boolean priority)
    {
        this(id, -1, priority);
    }

    public TrafficLight(int id, int parent, boolean priority)
    {
        this.parent = parent;
        this.id = id;
        this.priority = priority;
        SetStatus(Status.RED);
        System.out.println("id: " + this.id + "\tparent: " + this.parent);
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

    public void SetStatus(Status status)
    {
        this.status = status.ordinal();
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

    private void RunMaxRedTimerThread()
    {
        if(maxRedTimerThread == null)
        {
            maxRedTimerThread = new MaxRedTimerThread();
            maxRedTimerThread.start();
        }
    }

    private void DestroyMaxRedTimerThread()
    {
        maxRedTimerThread = null;
        SetSecsOnRed(0);
    }

    private class MaxRedTimerThread extends Thread
    {

        @Override
        public void run()
        {
            while(true)
            {
                try {
                    System.out.println("ping from " + GetID() + "\t" + GetSecsOnRed() + " secs on red");
                    sleep(1000);
                    int secsOnRed = GetSecsOnRed();
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
