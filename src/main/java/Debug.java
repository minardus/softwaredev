import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class Debug {

    private JFrame f;
    private JPanel panel = new JPanel();
    private JButton debugBtn = null;
    private JTable table = null;
    private Socket socket;
    private BufferedWriter out = null;
    private BufferedReader in = null;
    private boolean disconnected = false;

    public Debug()
    {
        f = new JFrame("SERVER_GUI");
        f.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        debugBtn = new JButton("DEBUG!");
        debugBtn.addActionListener(e -> new Thread() { public void run() { DebugThread(); } }.start());
        panel.add(debugBtn);

        JButton sendTestBtn = new JButton("send Test");
        sendTestBtn.addActionListener(e -> SendBaan(5, true));
        panel.add(sendTestBtn);

        JButton sendTestBtn2 = new JButton("send Test 2");
        sendTestBtn2.addActionListener(e -> SendBaan(5, false));
        panel.add(sendTestBtn2);

        String[] columns = new String[] {"Stoplicht", "Bezet", "Status"};
        DefaultTableModel model = new DefaultTableModel() {
            @Override
            public boolean isCellEditable(int row, int column) {
                //all cells false
                return false;
            }
        };
        for(String column : columns) {
            model.addColumn(column);
        }
        table = new JTable(model);
        panel.add(new JScrollPane(table));

        f.setContentPane(panel);

        f.setSize(800, 600);
        //f.pack();
        f.setVisible(true);
    }

    public void DebugThread()
    {
        debugBtn.setEnabled(false);
        try
        {
            socket = new Socket("127.0.0.1", Settings.Port);
            out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            Thread.sleep(1000); // wait a second, because server sends stuff first
            this.RecvHandler();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    private void SendBaan(int id, boolean bezet)
    {
        try {
            out.write("{\"banen\": [{\"id\": \"" + id + "\", \"bezet\": \"" + bezet + "\"}]}");
            out.newLine();
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void RecvHandler()
    {
        while(!disconnected) {
            try {
                String ret = in.readLine();
                System.out.println("RECEIVED ON CLIENT SIDE");
                System.out.println(ret);
                this.ParseServerJSON(ret);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void ParseServerJSON(String json)
    {
        //do something with json from server
        table.removeAll();
        DefaultTableModel model = (DefaultTableModel) table.getModel();

        // parse json
        JSONParser parser = new JSONParser();
        JSONObject jsonObject = null;
        try {
            Object obj = parser.parse(json);
            jsonObject = (JSONObject) obj;
        } catch (ParseException e) {
            e.printStackTrace();
        }

        JSONArray stoplichten = null;
        try {
            stoplichten = (JSONArray) jsonObject.get("stoplichten");
        } catch(Exception e){
            e.printStackTrace();
        }

        // parse banen
        if(stoplichten != null)
        {
            for (Object stoplicht : stoplichten) {
                JSONObject test = (JSONObject) stoplicht;
                final int status = Integer.parseInt(((String) test.get("status")));
                final int id = Integer.parseInt(((String) test.get("id")));
                model.addRow(new Object[] { id, false, status });
                //System.out.println("bezet: " + bezet + "\tid: " + id);
            }
        }
    }

}
