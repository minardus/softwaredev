import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class Debug extends Thread {

    private JFrame f;
    private JPanel panel = new JPanel();
    JButton debugBtn = null;
    private Socket socket;
    private BufferedWriter out = null;
    private BufferedReader in = null;

    public Debug()
    {
        f = new JFrame("SERVER_GUI");
        f.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        debugBtn = new JButton("DEBUG!");
        debugBtn.addActionListener(e -> run());
        panel.add(debugBtn);

        JButton sendTestBtn = new JButton("send Test");
        sendTestBtn.addActionListener(e -> SendBaan(5, true));
        panel.add(sendTestBtn);

        JButton sendTestBtn2 = new JButton("send Test 2");
        sendTestBtn2.addActionListener(e -> SendBaan(5, false));
        panel.add(sendTestBtn2);

        f.setContentPane(panel);

        f.setSize(800, 600);
        //f.pack();
        f.setVisible(true);
    }

    @Override
    public void run()
    {
        debugBtn.setEnabled(false);
        try
        {
            socket = new Socket("127.0.0.1", Settings.Port);
            out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
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

    private void ReadHandler()
    {

    }

}
