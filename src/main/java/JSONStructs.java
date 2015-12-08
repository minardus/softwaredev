import org.json.simple.JSONObject;

public class JSONStructs {

    public static final class Stoplichten {
        public int id;
        public int status;

        public Stoplichten(int id, int status)
        {
            this.id = id;
            this.status = status;
        }

        public JSONObject toJSONObject()
        {
            JSONObject obj = new JSONObject();
            obj.put("id", "" + id);
            obj.put("status", "" + status);
            return obj;
        }

        public String toJSONString(){
            StringBuffer sb = new StringBuffer();

            sb.append("{");

            sb.append(JSONObject.escape("id"));
            sb.append(":");
            sb.append(id);

            sb.append(",");

            sb.append(JSONObject.escape("status"));
            sb.append(":");
            sb.append(status);

            sb.append("}");

            System.out.println(sb.toString());

            return sb.toString();
        }
    }

    public static final class Banen {
        public int id;
        public boolean bezet;

        public Banen(int id, boolean bezet)
        {
            this.id = id;
            this.bezet = bezet;
        }

        public String toJSONString(){
            StringBuffer sb = new StringBuffer();

            sb.append("{");

            sb.append(JSONObject.escape("id"));
            sb.append(":");
            sb.append(id);

            sb.append(",");

            sb.append(JSONObject.escape("bezet"));
            sb.append(":");
            sb.append(bezet);

            sb.append("}");

            return sb.toString();
        }
    }

}
