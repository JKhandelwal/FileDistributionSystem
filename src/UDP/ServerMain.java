package UDP;

import java.io.File;
import java.io.FileInputStream;
import java.net.*;
import java.nio.ByteBuffer;
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;

public class ServerMain {
    private static DatagramPacket packet;
    private static MulticastSocket s;
    private static InetAddress group;
    private static ServerSocket serverSocket;
    private static int low = 0;
    private static int high = 0;
    private static int numberPacketsPerSection = 500;
    private static HashMap<Integer, byte[]> readBuffer = new HashMap<>();

    public static void main(String[] args) {
        try {
            String stringIP = InetAddress.getLocalHost().getHostAddress();
            //IP 228.5.6.7 and port 2345 was chosen for the MultiCast group.
            group = InetAddress.getByName(UDP.Config.ip);
            s = new MulticastSocket(UDP.Config.port);
            s.joinGroup(group);
            //Constructs the Datagram packet
            packet = new DatagramPacket(stringIP.getBytes(), stringIP.length(), group, UDP.Config.port);
            ServerIP ip = new ServerIP(s, packet);
            ip.start();

            BlockingQueue<ArrayList<Integer>> queue = new LinkedBlockingDeque<>(UDP.Config.NUMBER);
            List<ServerControl> servers = new LinkedList<>();

            serverSocket = new ServerSocket(UDP.Config.controlPort);
            System.out.println("Server - Your control server has started on port " + UDP.Config.controlPort);
            int count = 0;

            int numberSections;

            File f = new File(UDP.Config.filePath);

            numberSections = (int) Math.ceil((double) f.length() / (double) (numberPacketsPerSection * UDP.Config.sendSize));

            int numberOfBytesInLastSection = (int) (f.length() % (numberPacketsPerSection * UDP.Config.sendSize));

            int numberOfPacketsInLastSection = (int) Math.ceil((double) numberOfBytesInLastSection / (double) UDP.Config.sendSize);

            FileInputStream is = new FileInputStream(f);
            int packetCount = 1;

            int currentNum = UDP.Config.NUMBER;
            System.out.println("the number of sections is " + numberSections);

            while (count < UDP.Config.NUMBER) { // count to 3
                //Accepts the connection and starts a connection handler thread to manage that client.
                Socket connectionSocket = serverSocket.accept();
                ServerControl s = new ServerControl(connectionSocket, new File(UDP.Config.filePath), UDP.Config.sendSize, queue);
                s.start();
                servers.add(s);
                count++;
                System.out.println("Server - Accepted the connection to " + connectionSocket.getInetAddress());
            }

            ip.end();
            Thread.sleep(100);


            HashSet<Integer> sendSet = new HashSet<>();
            for (int i = 0; i < numberSections; i++) {
                queue.clear();
                System.out.println(i);
                low = high + 1;
                if (i == numberSections - 1) {
                    high += numberOfPacketsInLastSection;
                } else high += numberPacketsPerSection;

                readBuffer.clear();
                sendSet.clear();
                for (int j = low; j <= high; j++) {
                    byte[] b = new byte[UDP.Config.sendSize + Integer.BYTES];
                    int t = is.read(b, Integer.BYTES, UDP.Config.sendSize);
                    if (t == 0) {
                        System.out.println("yeah the read isnt great");
                    }
//                    System.out.println("server - Packet Number " + j);
                    ByteBuffer buffer = ByteBuffer.allocate(Integer.BYTES);
                    buffer.putInt(j);
                    byte[] num = buffer.array();
                    System.arraycopy(num, 0, b, 0, Integer.BYTES);
//                    if (j ==1){
//                        System.out.println(Arrays.toString(b));
//                    }
                    readBuffer.put(j, b);
                }

                /*read the requisite number of files
                put them in a hashmap
                loop to 3?
                set the section in the threads
                */
                for (int j = 0; j < 3; j++) {
                    //
                    servers.forEach(server -> server.latchContinue(low, high));
///                   Corresponds to 1
                    Thread.sleep(5);
                    if (j == 0) {
                        sendSet.clear();
                        sendSet.addAll(readBuffer.keySet());
                    } else {
//                        int counter =0;
//                        while (queue.size() ==0) {
//                            Thread.sleep(1000);
//                            if (counter == 10) break;
//                            counter ++;
//                        }
//                        for (int k = 0; k < 10; k++) {
//                            Thread.sleep(5);
//                            try {
//                                if (queue.size() == currentNum) break;
//                            } catch (Exception e) {
//                                System.out.println(i);
//                                System.exit(1);
//                            }
//
//                        }
                        while(true){
                            Thread.sleep(50);
                            if (queue.size() == UDP.Config.NUMBER) break;
                        }

                        sendSet.clear();
                        currentNum = queue.size();
                        queue.stream().filter(q -> q.size() > 0).forEach(a -> {
                            System.out.println("Server Size received is " + a.size());
//                            System.out.println("here? " + a);
                            try {
                                if (a.size() != 0) sendSet.addAll(a);
                            } catch (Exception e) {
                                System.out.println("gaaaaaaaaah");
                                System.exit(1);
                            }
                        });
//                        queue.drainTo(new ArrayList<>());
                        queue.clear();
                        assert queue.isEmpty();

                    }

                    if (sendSet.size() == 0) break;

                    int finalI = i;
                    sendSet.forEach(a -> {
                        try {
//                            if (a ==1){
//                                System.out.println(Arrays.toString(readBuffer.get(a)));
//                            }
//                            System.out.println("sending this " + a);
                            Thread.sleep(10);
                            packet = new DatagramPacket(readBuffer.get(a), (readBuffer.get(a)).length, group, UDP.Config.port);
                            s.send(packet);
//                            System.out.println("Server - sent" + a);
                        } catch (Exception e) {
                            System.out.println("final i error" + finalI);
                            e.printStackTrace();
                            System.exit(1);
                        }
                    });

                    Thread.sleep(1000);
                    //2
                    servers.forEach(server -> server.latchContinue(low, high));
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

            servers.forEach(s -> s.setExitLatch());
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
