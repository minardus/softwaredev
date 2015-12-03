import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.util.*;

public class Intersection {

    private Server server = null;
    private List<Boolean[]> states = null;
    private List<TrafficLight> trafficLights = null;

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

    public Intersection(List<TrafficLight> trafficLights, List<Boolean[]> states, Server server) {
        // set traffic lights
        this.trafficLights = trafficLights;

        // set states
        this.states = states;

        // set Server object
        this.server = server;
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

    public List<Boolean[]> GetStates()
    {
        return states;
    }

    public PriorityQueue<TrafficLight> GetPriorityQueue()
    {
        return trafficLightQueue;
    }

    public JSONObject GetTrafficLightsJSONObject()
    {
        JSONArray jsonTrafficLights = new JSONArray();
        trafficLights.forEach(trafficLight -> jsonTrafficLights.add(trafficLight.getJSONCompat()));
        JSONObject obj = new JSONObject();
        obj.put("stoplichten", jsonTrafficLights);
        return obj;
    }

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
                final boolean bezet = Boolean.parseBoolean(((String) test.get("bezet")));
                final int id = Integer.parseInt(((String) test.get("id")));
                trafficLights.stream()
                        .filter(t -> t.GetID() == id)
                        .findFirst()
                        .ifPresent(t -> t.SetOccupied(bezet));
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
                        .ifPresent(t -> t.SetOccupied(bezet));
                System.out.println("bezet: " + bezet + "\tid: " + id);
            }
        }
    }

}