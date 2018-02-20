package UDP;

import java.io.*;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.Socket;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class ClientMain {
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
            group = InetAddress.getByName(UDP.Config.ip);
            s = new MulticastSocket(UDP.Config.port);
            s.joinGroup(group);
            //Gets the DataGram socket
            byte[] buf = new byte[64];
            DatagramPacket recv = new DatagramPacket(buf, buf.length);
            s.receive(recv);
            System.out.println(Arrays.toString(recv.getData()));
            String host = new String(recv.getData()).trim();

            socket = new Socket(host, UDP.Config.controlPort);
            System.out.println("Client connected to " + host + " on port " + UDP.Config.controlPort + ".");
            pw = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()));
            is = socket.getInputStream();
            br = new BufferedReader(new InputStreamReader(is));

            String line = br.readLine();
            f = new File("/cs/scratch/jk218/" + line);
            System.out.println("client received: " + line);
            FileOutputStream fos = new FileOutputStream(f);
            line = br.readLine();
            System.out.println("client received: " + line);
            length = Long.parseLong(line);

            line = br.readLine();
            packetSize = Integer.parseInt(line);
            System.out.println("client received: " + line);


            finalPacketSize = (int) (length % packetSize);

            numPackets = (int) (length / packetSize);



            pw.println("receivedStart");
            pw.flush();
            int currLow = -1;
            int currHigh = 0;
            List<Integer> nums;
            ConcurrentLinkedQueue<Integer> range = new ConcurrentLinkedQueue<>();
            ConcurrentHashMap<Integer,byte[]> map = new ConcurrentHashMap<>();
            ClientReceive c = new ClientReceive(packetSize,s,map,range);
            ClientWriteFile fw = new ClientWriteFile(fos,packetSize);
            fw.start();
            c.start();
            boolean sectionComplete = false;
            boolean first = true;
            //TODO figure out where/how to write the file sections.
            while (true) {

                System.out.println("client- stuck here");
                line = br.readLine();
                String[] split = line.split(",");
                System.out.println("the split gives " + Arrays.toString(split));
                if (split[0].equals("sending")) {
                    System.out.println("client: gets here");

                    if (currLow != Integer.parseInt(split[1])){
                        System.out.println("client - unequal");
                        if (first){
                            first = false;
                        } else{
                            HashMap<Integer,byte[]> m = new HashMap<>();
                            m.putAll(map);
                            System.out.println("Client - size is " + m.size());
                            fw.countDown(m,currLow,currHigh);
                            fw.reset();
                        }

                        sectionComplete = false;

                        map.clear();
                        synchronized (range){
                        range.clear();
                        }
                       nums = IntStream.rangeClosed(Integer.parseInt(split[1]), Integer.parseInt(split[2]))
                                .boxed().collect(Collectors.toList());
                        range.addAll(nums);

                        currLow = Integer.parseInt(split[1]);
                       currHigh = Integer.parseInt(split[2]);
                    }

                    if (!sectionComplete) {
//                        System.out.println("Client - range before" + range);
//                        c.countdown((ArrayList<Integer>) range);
//                        System.out.println("Client - Range after" + range);
//                        c.resetLatch();
                    }

                    line = br.readLine();
//                    System.out.println("read the retransmit");

                    if (line.equals("retransmit")){
                        Thread.sleep(500);
                        System.out.println("sends size of " + range.size());
                        pw.println(range.size());
                        pw.flush();
                        if (range.size() == 0){
                            sectionComplete = true;
                            System.out.println("section is complete");
                            //todo here maybe?
                        } else {
                            System.out.println("didnt get " + range.toString());
                            synchronized (range){
                            range.forEach(a -> {
                                pw.println(a);
                                pw.flush();
                            });}
                        }
                    } else {
                        System.out.println("Broken Protocol exiting");
                    }



                } else {

                    if (split[0].equals("exit")){
                        c.end();
                        HashMap<Integer,byte[]> m = new HashMap<>();
                        m.putAll(map);
                        fw.countDown(m,currLow,currHigh);
                        fw.end();
                        //doexit things
                        break;
                    } else {


                    System.out.println("Protocol broken exiting");
                    }
//                cleanup();
                }

            }


        /*

        connectToServer();
        getInitMessage();
        replyWithAck();


        gets the range
        Listens to the packets and stores them
        gets the message to transmit the files which it needs again
        retransmits the files which it needs
        gets a message to listen again
           -> could be to ignore if all of the ranges are met
           -> gets the packages
        will ask again with the message * 2
            retransmits the files which it needs
        gets a message to listen again
           -> could be to ignore if all of the ranges are met
           -> gets the packages

         on last occasion it will be told to get fucked
         write the file to disk after it gets everything
         transmit the next section
         transmits the end character
          keeps the file

             */

//
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
