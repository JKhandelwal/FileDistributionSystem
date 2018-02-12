import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.nio.ByteBuffer;
import java.util.Arrays;

public class Client extends Thread {
    private static MulticastSocket s;
    private static InetAddress address;

    /**
     * Starts the Thread to run the MultiCasting
     */
    public void run() {
        try {
            //Gets the IP address of the program.
            address = InetAddress.getByName(Config.ip);
            //Joins the MultiCast group.
            s = new MulticastSocket(Config.port);
            s.joinGroup(address);
            receive();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    /**
     * Shutdown  function to close cleanly
     */
    public static void close() {
        try {
            s.leaveGroup(address);
        } catch (IOException e) {
            e.printStackTrace();
        }
        s.close();
    }

    /**
     * Receives the UDP packets.
     */
    private static void receive() {
        int counter = 0;
        try {

            while (true) {
                //Gets the DataGram socket
                byte[] buf = new byte[Config.sendSize + Long.BYTES*2];
                DatagramPacket recv = new DatagramPacket(buf, buf.length);
//                System.out.println("got here client");
                s.receive(recv);
//                System.out.println(new String(recv.getData()).trim());
//                System.out.println("received shit");
                byte [] t = Arrays.copyOfRange(recv.getData(),Config.sendSize +Long.BYTES,Config.sendSize + Long.BYTES*2);
//                System.out.println(t.length);
                ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES);
                buffer.put(t);
                buffer.flip();//need flip
                System.out.println("final number is received " + buffer.getLong());

                ByteBuffer buffer2 = ByteBuffer.allocate(Long.BYTES);
                buffer2.put(t);
                buffer2.flip();//need flip
                System.out.println("current number is " + buffer2.getLong() + "\n\n");

                if (buffer2.getLong() == buffer.getLong() -1){
                    System.out.println("YEAH IT WORKS AND REACHES THE END AND STUFF");
                    Thread.sleep(1000);
                    break;
                }
//                System.out.println(recv.getAddress());
//                System.out.println("Client?");

                //Since the server part of this program also sends the UDP packet, this code filters out the
                //The users own IP


            }
            close();
            System.out.println("it closed as well and stuff");
        } catch (Exception e){
            e.printStackTrace();
        }
    }


}
