import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.util.*;
import java.util.stream.Collectors;

public class Intersection {

    private static Server server = null;
    private List<boolean[]> states = null;
    private List<TrafficLight> trafficLights = null;

    public static void ServerCallback()
    {
        server.SendHandler();
    }

    // used as a temp queue to know the order of the traffic lights based on secsOnRed & priority
    private PriorityQueue<TrafficLight> trafficLightQueue = new PriorityQueue<>(20, new Comparator<TrafficLight>() {

        @Override
        public int compare(TrafficLight tl1, TrafficLight tl2) {
            // for safety do priority by ID
            boolean byID = true;    // if priority is done by ID
            int prio1 = tl1.GetID();
            int prio2 = tl2.GetID();
            // this below should always be happening, priority on whose first
            if(tl1.GetSecsOnRed() != tl2.GetSecsOnRed())
            {
                byID = false;
                prio1 = tl1.GetSecsOnRed();
                prio2 = tl2.GetSecsOnRed();
            }
            return (tl1.GetPriority() == tl2.GetPriority())
                    ? (byID ? (Integer.valueOf(prio1).compareTo(prio2)) : (Integer.valueOf(prio2).compareTo(prio1)))
                    : (tl1.GetPriority() ? -1 : 1);
        }

    });

    public Intersection(List<TrafficLight> trafficLights, List<boolean[]> states, Server server) {
        // set traffic lights
        this.trafficLights = trafficLights;

        // set states
        this.states = states;

        // set Server object
        this.server = server;

        // start update
        new Thread() {
            public void run() {
                while(true) {
                    try {
                        sleep(1000);   // loop through priorityqueue
                        update();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }.start();
    }

    public void StartStateTimer(Boolean[] state)
    {
        new Thread() {
            public void run() {
                try {
                    // set status of every traffic light
                    server.SendHandler(); // send current state to client via SendHandler()
                    sleep(Settings.OrangeTime);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }

    public List<TrafficLight> GetTrafficLights()
    {
        return trafficLights;
    }

    public List<boolean[]> GetStates()
    {
        return states;
    }

    public PriorityQueue<TrafficLight> GetPriorityQueue()
    {
        return trafficLightQueue;
    }

    /**
     * Return the trafficlights as JSON Object (for sending to client)
     * @return json object
     */
    public JSONObject GetTrafficLightsJSONObject()
    {
        JSONArray jsonTrafficLights = new JSONArray();
        trafficLights.forEach(trafficLight -> jsonTrafficLights.add(trafficLight.getJSONCompat()));

        JSONObject obj = new JSONObject();
        obj.put("stoplichten", jsonTrafficLights);
        return obj;
    }

    /**
     * Parse the JSON from the client, and do something with it immediately
     * @param json
     */
    public void ParseClientJSON(String json)
    {
        JSONParser parser = new JSONParser();
        JSONObject jsonObject = null;
        try {
            Object obj = parser.parse(json);
            jsonObject = (JSONObject) obj;
        } catch (ParseException e) {
            e.printStackTrace();
        }

        JSONArray banen = null;
        JSONArray busbanen = null;
        try {
            banen = (JSONArray) jsonObject.get("banen");
        } catch(Exception e){
            e.printStackTrace();
        }
        try {
            busbanen = (JSONArray) jsonObject.get("busbanen");
        } catch(Exception e) {
            e.printStackTrace();
        }

        // parse banen
        if(banen != null)
        {
            for (Object baan : banen) {
                JSONObject test = (JSONObject) baan;
                final boolean bezet = Boolean.parseBoolean(("" + test.get("bezet")));
                final int id = Integer.parseInt(("" + test.get("id")));
                trafficLights.stream()
                        .filter(t -> t.GetID() == id)
                        .findFirst()
                        .ifPresent(t -> addToQueue(t, bezet));
                System.out.println("bezet: " + bezet + "\tid: " + id);
            }
        }

        // parse banen
        if(busbanen != null)
        {
            for (Object baan : busbanen) {
                JSONObject test = (JSONObject) baan;
                final boolean bezet = Boolean.parseBoolean(((String) test.get("bezet")));
                final int id = Integer.parseInt(((String) test.get("id")));
                trafficLights.stream()
                        .filter(t -> t.GetID() == id)
                        .findFirst()
                        .ifPresent(t -> addToQueue(t, bezet));
                System.out.println("bezet: " + bezet + "\tid: " + id);
            }
        }
    }

    /**
     * Get the parent of a traffic light (which isn't a parent)
     * @param id
     * @return
     */
    private TrafficLight GetTrafficLightParent(int id)
    {
        return trafficLights.stream()
                .filter(t -> t.GetID() == id)
                .findFirst().get();
    }

    private List<TrafficLight> GetTrafficLightKids(int parentId)
    {
        return trafficLights.stream()
                .filter(t -> t.GetParent() == parentId)
                .collect(Collectors.toList());
    }

    /**
     * Add a trafficlight in the PriorityQueue and set its occupation
     * Only add a trafficlight when its occupied, the Queue is for occupied trafficlights only
     * Also only add the parent traffic light, the kids will be handled!
     * @param tl
     * @param bezet
     */
    private void addToQueue(TrafficLight tl, boolean bezet)
    {
        tl.SetFakeOccupied(bezet);  // 3 komt binnen
        TrafficLight parent = tl.GetParent() == -1 ? tl : GetTrafficLightParent(tl.GetParent()); // ik pak 2
        System.out.println("WTF: " + parent.GetID());
        List<TrafficLight> kids = GetTrafficLightKids(parent.GetID()); // en 3

        // als er ook maar 1 fakeOccupied in parent of kids true is .. dan is bezet true .. anders false
        if(!parent.GetFakeOccupied()) {
            for(TrafficLight kid : kids) {
                if(kid.GetFakeOccupied()) {
                    bezet = true;
                    break;
                }
            }
        }
        parent.SetOccupied(bezet);
        for(TrafficLight kid : kids)
            kid.SetOccupied(bezet);
        if(bezet && !trafficLightQueue.contains(parent) && parent.GetStatus() == TrafficLight.Status.RED.ordinal())
            trafficLightQueue.add(parent);
    }

    /**
     * This method is going to handle the trafficLightQueue which holds the traffic lights which need to be handled
     * Then via a algorithm which checks which traffic lights may go on green, the status of these traffic lights
     * is set and sent to the client.
     * Only send to client if nesccesary (priorityqueue.size() > 0) and (tls.size() > 0)
     */
    private void update()
    {
        if(GetPriorityQueue().size() > 0) {
            List<TrafficLight> tls = GetPossibleState();
            for (TrafficLight tl : tls) {
                tl.SetStatus(TrafficLight.Status.GREEN);
                for(TrafficLight kid : GetTrafficLightKids(tl.GetID())) {
                    kid.SetStatus(TrafficLight.Status.GREEN);
                }
            }
            if (tls.size() > 0)
                server.SendHandler();
        }
    }

    /**
     * This function gives a true or false if a list of traffic lights can ALL be green
     */
    private List<TrafficLight> GetPossibleState()
    {
        /* copy priorityqueue to normal list */
        List<TrafficLight> fromQueueTls = new ArrayList<>();
        for(TrafficLight tl : GetPriorityQueue())
        {
            fromQueueTls.add(tl);
        }
        // check if possible, else remove last item from list (till its possible)
        boolean isPossible = false;
        while(!isPossible)
        {
            // if true return the arraylist and remove the items from the real queue
            if(PossibleAlgorithm(fromQueueTls)) {
                fromQueueTls.forEach(trafficLightQueue::remove);    // remove from priorityqueue and add to list
                isPossible = true;
            // else remove last item and try again
            } else  {
                fromQueueTls.remove(fromQueueTls.size() - 1);
            }
        }
        return fromQueueTls;    // return the list with trafficlights which can hop on green
    }

    /**
     * Check if List of traffic lights are allowed to hop on Green at once
     * @param tls
     * @return
     */
    private boolean PossibleAlgorithm(List<TrafficLight> tls)
    {
        boolean[] state = states.get(tls.get(0).GetFakeID());
        boolean possible = true;

        // check if it is possible without checking on Red lights
        if(tls.size() > 1) {
            for (TrafficLight tl : tls) {
                if (!state[tl.GetFakeID()]) {
                    possible = false;
                    break;
                }
            }
        }

        // then check if all the falses in the state are on RED
        if(possible) {
            int i = 0;
            for(boolean allowed : state) {
                TrafficLight tl = trafficLights.stream()
                        .filter(t -> t.GetFakeID() == i)
                        .findFirst()
                        .get();
                // if not allowed and it is not on red
                if(!allowed && tl.GetStatus() != TrafficLight.Status.RED.ordinal()) {
                    possible = false;
                    break;
                }
            }
        }
        return possible;
    }

}
