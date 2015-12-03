import javax.swing.*;

public class Main {

    /**
     * Start the program
     *
     * @param args no args plz, could be states file.. but noh, hardcoded that for now
     */
    public static void main(String... args) {
        try {
            // start to serve the people
            System.out.println("create traffic lights");
            Server server = new Server();
            Debug debug = new Debug(server);
            server.run();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

}