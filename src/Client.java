import java.io.FileOutputStream;
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


        try {
            FileOutputStream fos = new FileOutputStream(Config.outputFile);
            while (true) {
                //Gets the DataGram socket
                byte[] buf = new byte[Config.sendSize + Long.BYTES*2];
                DatagramPacket recv = new DatagramPacket(buf, buf.length);
                s.receive(recv);

                ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES);
                buffer.put(recv.getData(),0,Long.BYTES);
                buffer.flip();
                long curr =  buffer.getLong();
                System.out.println("current number is received " + curr);

                ByteBuffer buffer2 = ByteBuffer.allocate(Long.BYTES);
                buffer2.put(recv.getData(),Long.BYTES,Long.BYTES);
                buffer2.flip();
                long total = buffer2.getLong();
                System.out.println("final number is " + total + "\n");

                fos.write(recv.getData(),Long.BYTES *2, Config.sendSize);
                if (curr == total) {
//                    byte[] last = Arrays.copyOfRange(recv.getData(),Long.BYTES *2,Long.BYTES*2 + Config.sendSize);
//
//                    System.out.println("YEAH IT WORKS AND REACHES THE END AND STUFF");
//                    for (int i =0; i < last.length; i++){
//                        if (last[i] != 0){
//                            fos.write(last[i]);
//                        } else{
//                            break;
//                        }
//                    }
                    Thread.sleep(1000);
                    break;
                } else {

                }

                System.out.println("loop");
            }
            close();
            fos.close();
            System.out.println("it closed as well and stuff");
        } catch (Exception e){
            e.printStackTrace();
        }
    }


}
