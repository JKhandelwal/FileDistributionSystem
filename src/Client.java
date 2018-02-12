import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;

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
                byte[] buf = new byte[256];
                DatagramPacket recv = new DatagramPacket(buf, buf.length);
                System.out.println("got here client");
                s.receive(recv);
                System.out.println(new String(recv.getData()).trim());
                System.out.println(recv.getAddress());
                System.out.println("Client?");

                //Since the server part of this program also sends the UDP packet, this code filters out the
                //The users own IP


            }
        } catch (Exception e){
            e.printStackTrace();
        }
    }


}
