import java.io.*;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
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
            group = InetAddress.getByName(Config.ip);
            s = new MulticastSocket(Config.port);
            s.joinGroup(group);
            //Gets the DataGram socket
            byte[] buf = new byte[64];
            DatagramPacket recv = new DatagramPacket(buf, buf.length);
            s.receive(recv);
            String host = new String(recv.getData()).trim();

            socket = new Socket(host, Config.controlPort);
            System.out.println("Client connected to " + host + " on port " + Config.controlPort + ".");
            pw = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()));
            is = socket.getInputStream();
            br = new BufferedReader(new InputStreamReader(is));

            String line = br.readLine();
            f = new File("/cs/scratch/jk218/" + line);

            line = br.readLine();
            length = Long.parseLong(line);

            line = br.readLine();
            packetSize = Integer.parseInt(line);

            finalPacketSize = (int) (length % packetSize);

            numPackets = (int) (length / packetSize);


            ClientReceive c = new ClientReceive();

            pw.println("receivedStart");
            int currLow = -1;
            int currHigh = 0;
            List<Integer> range = new ArrayList<>();
            while (true) {


                line = br.readLine();
                String[] split = line.split(",");
                if (split[0].equals("sending")) {

                    if (currLow != Integer.parseInt(split[1])){
                        range.clear();
                       range = IntStream.rangeClosed(Integer.parseInt(split[1]), Integer.parseInt(split[1]))
                                .boxed().collect(Collectors.toList());
                    } else{

                    }


                } else {
                    System.out.println("Protocol broken exiting");
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
