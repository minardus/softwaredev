import static org.junit.Assert.assertEquals;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class IntersectionTest {

    List<TrafficLight> trafficLights = new ArrayList<TrafficLight>();
    Intersection intersection = null;

    @Test
    public void testPriorityQueue()
    {
        // add some trafficlights with different priorities
        trafficLights.add(new TrafficLight(7, -1, false));
        trafficLights.add(new TrafficLight(14, -1, true));
        trafficLights.add(new TrafficLight(3, -1, false));
        trafficLights.add(new TrafficLight(4, -1, false));
        trafficLights.add(new TrafficLight(5, -1, true));

        // create a basic intersection
        intersection = new Intersection(trafficLights, null, null);

        // add to priority list, dont use secsOnRed
        for(TrafficLight tl : trafficLights) {
            intersection.GetPriorityQueue().add(tl);
        }

        // assert statements
        assertEquals("this should be TrafficLight #5", 5, intersection.GetPriorityQueue().poll().GetID());
        assertEquals("this should be TrafficLight #14", 14, intersection.GetPriorityQueue().poll().GetID());
        assertEquals("this should be TrafficLight #3", 3, intersection.GetPriorityQueue().poll().GetID());
        assertEquals("this should be TrafficLight #4", 4, intersection.GetPriorityQueue().poll().GetID());
        assertEquals("this should be TrafficLight #7", 7, intersection.GetPriorityQueue().poll().GetID());

        // simulate, add to priority list while also using secsOnRed
        Thread testThread = new TestThread();
        testThread.start();
        while(testThread.isAlive())
        {
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        for(TrafficLight tl : trafficLights) {
            intersection.GetPriorityQueue().add(tl);
        }

        // assert statements
        assertEquals("this should be TrafficLight #14", 14, intersection.GetPriorityQueue().poll().GetID());
        assertEquals("this should be TrafficLight #5", 5, intersection.GetPriorityQueue().poll().GetID());
        assertEquals("this should be TrafficLight #7", 7, intersection.GetPriorityQueue().poll().GetID());
        assertEquals("this should be TrafficLight #3", 3, intersection.GetPriorityQueue().poll().GetID());
        assertEquals("this should be TrafficLight #4", 4, intersection.GetPriorityQueue().poll().GetID());

        // destroy MaxRedTimeThreads
        for(TrafficLight tl : trafficLights)
            tl.SetOccupied(false);
    }

    private class TestThread extends Thread
    {
        @Override
        public void run()
        {
            // add to priority queue
            for(TrafficLight tl : trafficLights) {
                try {
                    tl.SetOccupied(true);
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

}
