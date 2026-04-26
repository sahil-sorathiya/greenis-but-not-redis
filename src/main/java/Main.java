import resp.*;
import server.GreenisServer;

import java.io.IOException;

public class Main {
    public static void main(String[] args) throws IOException {
        System.out.println("Logs from greenis will appear here!");

        //: Parse arguments
        String dir = null;
        String dbFileName = null;

        for (int i = 0; i < args.length; i++) {
            switch (args[i]) {
                case "--dir":
                    if (i + 1 < args.length) {
                        dir = args[++i];
                    }
                    break;
                case "--dbfilename":
                    if (i + 1 < args.length) {
                        dbFileName = args[++i];
                    }
                    break;
            }
        }

        //: Configure Server
        int port = 6379;
        GreenisServer server = new GreenisServer();

        try {
            server.init(port, dir, dbFileName);
            server.bind();
            server.configureNonBlocking();
            server.registerAcceptEvent();
            server.handleEvents(); //: Infinite Loop
        } catch (IOException e) {
            server.close();
            System.out.println("IOException at Main: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            server.close();
            System.out.println("Exception at Main: " + e.getMessage());
            e.printStackTrace();
        }
    }
}

