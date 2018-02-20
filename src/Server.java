
import java.io.File;
import java.io.FileInputStream;
import java.io.RandomAccessFile;
import java.net.*;
import java.nio.ByteBuffer;
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;

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

            BlockingQueue<ArrayList<Integer>> queue = new LinkedBlockingDeque<>(Config.NUMBER);
            List<ServerControl> servers = new LinkedList<>();

            serverSocket = new ServerSocket(Config.controlPort);
            System.out.println("Server - Your control server has started on port " + Config.controlPort);
            int count = 0;

            File f = new File(Config.filePath);

            while (count < Config.NUMBER) { // count to 3
                //Accepts the connection and starts a connection handler thread to manage that client.
                Socket connectionSocket = serverSocket.accept();
                ServerControl s = new ServerControl(connectionSocket, new File(Config.filePath), Config.sendSize, queue);
                s.start();
                servers.add(s);
                count++;
                System.out.println("Server - Accepted the connection to " + connectionSocket.getInetAddress());
            }

            ip.end();

            Thread.sleep(500);
            servers.forEach(ServerControl::send);
            Thread.sleep(1000);


            FileInputStream is = new FileInputStream(f);
            byte[] send = new byte[Config.sendSize + Integer.BYTES];
            int chunkLen = 0;
            int currentNum = 1;
            while ((chunkLen = is.read(send, Integer.BYTES, Config.sendSize)) != -1) {
//                System.out.println("current Num " + currentNum);
                ByteBuffer buffer = ByteBuffer.allocate(Integer.BYTES);
                buffer.putInt(currentNum++);
                byte[] a = buffer.array();
                System.arraycopy(a, 0, send, 0, Integer.BYTES);
                packet = new DatagramPacket(send, send.length, group, Config.port);
                s.send(packet);
                System.out.println("server Chunk Length is " + chunkLen);
                Thread.sleep(1);
            }
            System.out.println("server final packet count is" + currentNum);
            System.out.println("file size is " + f.length());
            System.out.println("server - finished sending the file");


            servers.forEach(a -> {
                a.sendReTransmitMessage();
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                a.latchContinue();
            });

            System.out.println("Server - sent the re-transmit message");

            RandomAccessFile raf = new RandomAccessFile(f,"r");


            final int[] activeThreadCount = {Config.NUMBER};
            HashSet<Integer> sendSet = new HashSet<>();
            ByteBuffer buffer2 = ByteBuffer.allocate(Integer.BYTES);
            while (true) {
                System.out.println("In the Loop");

                if (activeThreadCount[0] == 0) break;

                System.out.println("Server - Waiting for the thing");
                while (true) {
                    Thread.sleep(50);
                    System.out.println(queue.size());
                    if (queue.size() == activeThreadCount[0]) break;
                }
                System.out.println("Server - After the thing");

                sendSet.clear();
                queue.forEach(a -> {
                    if (a.size() == 0) {
                        activeThreadCount[0]--;
                    } else {
                        System.out.println(a.size());
                        sendSet.addAll(a);
                    }
                });
                queue.clear();
                System.out.println("Server - Adds the thing to set");

                if (sendSet.size() == 0) {
                    System.out.println("Server - sendsize is FING 0");
                    break;
                }

                servers.forEach(ServerControl::send);
                System.out.println("Server - after sending");
                Thread.sleep(1000);

                sendSet.forEach(a -> {
                    try {
                        System.out.println("sending this " + a);
                        raf.seek((a-1) * Config.sendSize);
                        raf.read(send,Integer.BYTES,Config.sendSize);
                        buffer2.putInt(a);
                        byte[] b = buffer2.array();
                        buffer2.position(0);
                        System.arraycopy(b, 0, send, 0, Integer.BYTES);
                        packet = new DatagramPacket(send, send.length, group, Config.port);
                        s.send(packet);
//                        Thread.sleep(5);
//                            System.out.println("Server - sent" + a);
                    } catch (Exception e) {
                        e.printStackTrace();
                        System.exit(1);
                    }
                });
                System.out.println("Server - Before Transmit");
                servers.forEach(a -> {
                    a.sendReTransmitMessage();
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    a.latchContinue();
                });
                System.out.println("Server - After Transmit");


            }

            System.out.println("Omg it finished");


            is.close();
            raf.close();
            s.leaveGroup(group);

            return;

        } catch (Exception e) {
            System.out.println("Server could not start - Errors" + e.getMessage());
        }
    }

}
