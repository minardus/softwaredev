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

        JButton sendTestBtn = new JButton("SET 2 ON TRUE");
        sendTestBtn.addActionListener(e -> SendBaan(2, true));
        panel.add(sendTestBtn);

        JButton sendTestBtn2 = new JButton("SET 2 on FALSE");
        sendTestBtn2.addActionListener(e -> SendBaan(2, false));
        panel.add(sendTestBtn2);

        JButton sendTestBtn3 = new JButton("SET 3 on TRUE");
        sendTestBtn3.addActionListener(e -> SendBaan(3, true));
        panel.add(sendTestBtn3);

        JButton sendTestBtn4 = new JButton("SET 3 on FALSE");
        sendTestBtn4.addActionListener(e -> SendBaan(3, false));
        panel.add(sendTestBtn4);

        String[] columns = new String[] {"Stoplicht", "Status"};
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
        DefaultTableModel model = (DefaultTableModel) table.getModel();
        model.setRowCount(0);

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

        // parse stoplichten
        if(stoplichten != null)
        {
            for (Object stoplicht : stoplichten) {
                JSONObject test = (JSONObject) stoplicht;
                final int status = Integer.parseInt(("" +  test.get("status")));
                final int id = Integer.parseInt(("" +  test.get("id")));
                model.addRow(new Object[] { id, GetNamedStatus(status) });
                //System.out.println("bezet: " + bezet + "\tid: " + id);
            }
        }
    }


    private String GetNamedStatus(int status)
    {
        switch(status)
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

}
