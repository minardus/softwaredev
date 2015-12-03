import org.junit.Test;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class ParseClientJSONTest {

    @Test
    public void testParsingClientJSON()
    {
        // create intersection where ParseClientJSON function is
        Intersection intersection = null;
        try {
            intersection = new Intersection(readTrafficLights(), null, null);
        } catch (Exception e) {
            e.printStackTrace();
        }

        // test string
        String testJson = "{\"banen\":[{\"id\":\"5\",\"bezet\":\"true\"},{\"id\":\"1\",\"bezet\":\"true\"}]}";
        intersection.ParseClientJSON(testJson);

        // test busbanen
        testJson = "{\"banen\":[{\"id\":\"15\",\"bezet\":\"true\"},{\"id\":\"16\",\"bezet\":\"true\"}]}";
        intersection.ParseClientJSON(testJson);
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
