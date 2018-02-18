import java.io.File;
import java.io.IOException;
import java.net.*;
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;

public class ServerMain {
    private static DatagramPacket packet;
    private static MulticastSocket s;
    private static InetAddress group;
    private static ServerSocket serverSocket;
    private static int low;
    private static int high;
    private static int NumberOfPacketsPerSection =0;
    private static HashMap<Integer,byte[]> readBuffer = new HashMap<>();

    public static void main(String[] args) {
        try {
            String stringIP = InetAddress.getLocalHost().getHostAddress();
            //IP 228.5.6.7 and port 2345 was chosen for the MultiCast group.
            group = InetAddress.getByName(Config.ip);
            s = new MulticastSocket(Config.port);
            s.joinGroup(group);
            //Constructs the Datagram packet
            packet = new DatagramPacket(stringIP.getBytes(), stringIP.length(), group, Config.port);
            ServerIP ip = new ServerIP(s,packet);
            ip.start();

            BlockingQueue<ArrayList<Integer>> queue = new LinkedBlockingDeque<>(Config.NUMBER);
            List<ServerControl> servers = new LinkedList<>();

            serverSocket = new ServerSocket(Config.controlPort);
            System.out.println("Server - Your control server has started on port " + Config.controlPort);
            int count =0;
            //Calculate Number of Sections todo
            int numberSections = 7;
            while (count < Config.NUMBER) { // count to 3
                //Accepts the connection and starts a connection handler thread to manage that client.
                Socket connectionSocket = serverSocket.accept();
                ServerControl s = new ServerControl(connectionSocket,new File(Config.outputFile),Config.sendSize,queue);
                servers.add(s);
                count++;
                System.out.println("Server - Accepted the connection to " + connectionSocket.getInetAddress());
            }

            ip.countUp();

            HashSet<Integer> sendSet = new HashSet<>();
            for (int i=0;i < numberSections;i++){
                low = high + 1;
                high += NumberOfPacketsPerSection;
                //set low and high
                //TODO Sectioning
                //TODO figure where to read the file

                /*read the requisite number of files
                put them in a hashmap
                loop to 3?
                set the section in the threads
                */
                for (int j = 0; j < 3;j++){
                    //
                    servers.forEach(server->server.latchContinue(low,high));
///                   Corresponds to 1
                    Thread.sleep(500);
                    if (j ==0){
                        sendSet.addAll(readBuffer.keySet());
                    }else{
                        queue.stream().filter(q->q.size() > 0).forEach(a->sendSet.addAll(a));
//                        queue.drainTo(new ArrayList<>());
                        queue.clear();
                        assert queue.isEmpty();

                    }

                    if (sendSet.size() ==0) break;

                    sendSet.forEach(a -> {
                        try {
                            Thread.sleep(1);
                            s.send(new DatagramPacket(readBuffer.get(a), readBuffer.get(a).length, group, Config.port));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    });

                    Thread.sleep(500);
                    //2
                    servers.forEach(server->server.latchContinue(low,high));
                    Thread.sleep(1000);

                }
                /*
                  unlatch to send the message to transmit
                  wait 100ms
                  send the packets with 1ms gap
                  wait 1s
                  read the queue
                  if empty break
                  else loop having set the ones which need to be resent
                */
            }

            servers.forEach(s->s.setExitLatch());
            //undo the latch which allows the thing to send the end info

            //For Each Section

                //Transmit the files
                //wait a couple of seconds
                //count to 3 i guess
                    // Poll the blocking queue
                    // Open the latches to tell the person to transmit
                    // Retransmit the files




            
        } catch (Exception e) {
            System.out.println("Server could not start - Errors" + e.getMessage());
        }
    }

}
