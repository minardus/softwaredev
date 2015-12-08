import org.json.simple.JSONValue;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class Server extends Thread {

    private BufferedWriter out = null;
    private BufferedReader in = null;

    private Intersection intersection;
    private boolean disconnected = false;

    public Server() throws Exception
    {
        this.intersection = new Intersection( readTrafficLights(), readStates(), this );
    }

    @Override
    public void run()
    {
        try
        {
            ServerSocket server = new ServerSocket(Settings.Port);
            while(!disconnected)
            {
                System.out.println("wait for client...");
                Socket socket = server.accept();
                // we now have json, now send that shit
                out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                SendHandler();  // send the state for the first time
                RecvHandler();  // start recieve thread for receiving client states
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    /**
     * Call this when there is a change of state
     */
    public void SendHandler()
    {
        String json = intersection.GetTrafficLightsJSONObject().toJSONString();
        System.out.println(json);
        try {
            System.out.println("SENDING ON SERVER SIDE");
            out.write(json);
            out.newLine();  // they want it, they get it.. eat that return newline
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Read state that the client sends
     */
    public void RecvHandler()
    {
        while(!disconnected) {
            try {
                String ret = in.readLine();
                System.out.println(ret);
                intersection.ParseClientJSON(ret);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Read states from csv file
     * @return List of boolean arrays (possible states per trafficlight)
     * @throws Exception
     */
    public static List<Boolean[]> readStates() throws Exception
    {
        List<Boolean[]> states = new ArrayList<>();
        BufferedReader br = new BufferedReader(new FileReader("stoplichten_states.csv"));
        // read the first two lines and throw away
        br.readLine();br.readLine();
        String line;
        while ((line = br.readLine()) != null) {
            String[] row_str = line.split(";");
            // skip first item in row_str, so row_bool = -1, and for loop begins with 1
            Boolean[] row_bool = new Boolean[row_str.length - 1];
            for(int i=1; i<row_bool.length;i++)
                row_bool[i] = Boolean.parseBoolean(row_str[i]);
            states.add(row_bool);
        }
        br.close();
        return states;
    }

    /**
     * Read traffic lights from csv file for reusability
     * @return A list of traffic lights
     * @throws Exception
     */
    private List<TrafficLight> readTrafficLights() throws Exception
    {
        List<TrafficLight> trafficLights = new ArrayList<>();
        BufferedReader br = new BufferedReader(new FileReader("stoplichten_states.csv"));
        String[] linked = br.readLine().split(";");  // this lights are linked to other lights
        String[] lights = br.readLine().split(";");  // the lights with action
        // skip first in lights[], which is for readability purposes in excel
        for(int i=1;i<lights.length;i++) {
            int tlid = parseTrafficLightId(lights[i]);
            trafficLights.add(new TrafficLight(tlid, tlid < Settings.noPriorityThreshold));
        }
        // add linked lights, with parent id
        for(String link : linked) {
            String[] splitted = link.split("=");
            String parent = splitted[0];
            String[] children = splitted[1].split(",");
            for(String child : children) {
                int tlid = parseTrafficLightId(child);
                int ptlid = parseTrafficLightId(parent);
                trafficLights.add(new TrafficLight(tlid, ptlid, tlid < Settings.noPriorityThreshold));
            }
        }
        return trafficLights;
    }

    /**
     * Parse a traffic light ID
     * @param id String where ID is in S* where, * is ID
     * @return the parsed ID (integer form)
     */
    private int parseTrafficLightId(String id)
    {
        return Integer.parseInt(id.substring(1));
    }

}
