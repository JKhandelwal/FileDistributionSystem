import sun.awt.image.ImageWatched;

import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.LinkedList;
import java.util.List;

public class ServerMain {
    private static ServerSocket serverSocket;

    public static void main(String[] args) {
        try {

            List<ServerControl> servers = new LinkedList<>();

            serverSocket = new ServerSocket(Config.controlPort);
            System.out.println("Server - Your control server has started on port " + Config.controlPort);
            int count =0;
            while (count < Config.NUMBER) { // count to 3
                //Accepts the connection and starts a connection handler thread to manage that client.
                Socket connectionSocket = serverSocket.accept();
                ServerControl s = new ServerControl(connectionSocket,new File(Config.outputFile),Config.sendSize);
                servers.add(s);
                count++;
                System.out.println("Server - Accepted the connection to " + connectionSocket.getInetAddress());
            }


            for (ServerControl s:servers) {
                s.latchContinue();
            }


            Thread.sleep(1000);

            //For Each Section

                //Transmit the files
                //wait a couple of seconds
                //count to 3 i guess
                    // Poll the blocking queue
                    //Open the latches to tell the person to transmit
                    //Retransmit the files




            
        } catch (Exception e) {
            System.out.println("Server could not start - Errors" + e.getMessage());
        }
    }

}
