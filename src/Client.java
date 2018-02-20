import java.io.*;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Client {
    private static MulticastSocket s;
    private static InetAddress group;
    private static Socket socket;
    private static BufferedReader br;
    private static PrintWriter pw;
    private static InputStream is;
    private static File f;
    private static long length;
    private static int numPackets;
    private static int packetSize;
    private static int finalPacketSize;

    public static void main(String[] args) {

        try {
            group = InetAddress.getByName(Config.ip);
            s = new MulticastSocket(Config.port);
            s.joinGroup(group);
            //Gets the DataGram socket
            byte[] buf = new byte[64];
            DatagramPacket recv = new DatagramPacket(buf, buf.length);
            s.receive(recv);
            System.out.println(Arrays.toString(recv.getData()));
            String host = new String(recv.getData()).trim();

            socket = new Socket(host, Config.controlPort);
            System.out.println("Client connected to " + host + " on port " + Config.controlPort + ".");
            pw = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()));
            is = socket.getInputStream();
            br = new BufferedReader(new InputStreamReader(is));

            String line = br.readLine();
            f = new File("/cs/scratch/jk218/" + line);
            System.out.println("client received: " + line);
            RandomAccessFile fos = new RandomAccessFile(f,"rw");
            line = br.readLine();
            System.out.println("client received: " + line);
            length = Long.parseLong(line);

            line = br.readLine();
            packetSize = Integer.parseInt(line);
            System.out.println("client received: " + line);

            finalPacketSize = (int) (length % packetSize);
            numPackets = (int) Math.ceil((double) length / (double) packetSize);
            System.out.println("number of packets is " + numPackets);
            pw.println("receivedStart");
            pw.flush();

            List<Integer> nums;
            nums = IntStream.rangeClosed(1, numPackets)
                    .boxed().collect(Collectors.toList());
            ConcurrentLinkedQueue<Integer> range = new ConcurrentLinkedQueue<>(nums);
            ConcurrentLinkedQueue<Integer> writeQueue = new ConcurrentLinkedQueue<>();
            ConcurrentHashMap<Integer,byte[]> map = new ConcurrentHashMap<>();
            ClientReceive c = new ClientReceive(packetSize,s,map,range,writeQueue);
            ClientWriteFile fw = new ClientWriteFile(fos,packetSize,map,writeQueue,numPackets,finalPacketSize);

            line = br.readLine();
            if (line.equals("sending")){
                System.out.println("Client - Inside the first loop");
                fw.start();
                c.start();

                while (true) {
                    System.out.println("Client - Inside the While true");
                    line = br.readLine();
                    if (line.equals("retransmit")){
                        System.out.println("Client - Waiting for re-transmit");
                        Thread.sleep(500);
                        synchronized (range){
                            System.out.println("client - sending " + range.size());
                            pw.println(range.size());
                            pw.flush();
                            if (range.size() ==0) break;
                            range.forEach(a->{
                                pw.println(a);
                                pw.flush();
                            });
                        }
                        line = br.readLine();
                        if (!line.equals("sending")){
                            System.out.println("broken protocol");
                            break;
                        }
                    } else {
                        System.out.println("broken protocol");
                        break;
                    }
                }

                System.out.println("Client finished and It actually fing worked");
//                c.join();
                fw.end();
                s.leaveGroup(group);

            } else{
                System.out.println("Protocol Broken, aborting ");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
