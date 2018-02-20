package MultiThreadedTCP;

import UDP.Config;
import UDP.ServerIP;

import java.io.File;
import java.net.*;

/**
 * Created by jalajkhandelwal on 19/02/2018.
 */
public class Server {

    private static DatagramPacket packet;
    private static MulticastSocket s;
    private static InetAddress group;
    private static ServerSocket serverSocket;

    public static void main(String[] args) {
        try {
            String stringIP = InetAddress.getLocalHost().getHostAddress();
            //IP 228.5.6.7 and port 2345 was chosen for the MultiCast group.
            group = InetAddress.getByName(Config.ip);
            s = new MulticastSocket(Config.port);
            s.joinGroup(group);
            //Constructs the Datagram packet
            packet = new DatagramPacket(stringIP.getBytes(), stringIP.length(), group, Config.port);
            ServerIP ip = new ServerIP(s, packet);
            ip.start();

            serverSocket = new ServerSocket(Config.controlPort);
            System.out.println("Server - Your control server has started on port " + Config.controlPort);
            int count =0;
            while (count < Config.NUMBER) { // count to 3
                //Accepts the connection and starts a connection handler thread to manage that client.
                Socket connectionSocket = serverSocket.accept();
                TransferFiles s = new TransferFiles(connectionSocket,new File(Config.filePath));
                s.start();
                count++;
                System.out.println("Server - Accepted the connection to " + connectionSocket.getInetAddress());
            }
        } catch( Exception e){
            e.printStackTrace();
        }
    }

}
