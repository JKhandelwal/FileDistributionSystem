import java.io.FileOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class Client extends Thread {
    private static MulticastSocket s;
    private static DatagramPacket packet;
    private static InetAddress group;
    /**
     * Starts the Thread to run the MultiCasting
     */
    public void run() {
        try {
            //Gets the IP address of the program.
            group = InetAddress.getByName(Config.ip);
            //Joins the MultiCast group.
            s = new MulticastSocket(Config.port);
            s.joinGroup(group);
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
            s.leaveGroup(group);
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

                if (receiveStart()) {
                    //Gets the DataGram socket
                    byte[] buf = new byte[Config.sendSize + Long.BYTES * 2];
                    DatagramPacket recv = new DatagramPacket(buf, buf.length);
                    s.receive(recv);

                    ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES);
                    buffer.put(recv.getData(), 0, Long.BYTES);
                    buffer.flip();
                    long curr = buffer.getLong();
                    System.out.println("current number is received " + curr);

                    ByteBuffer buffer2 = ByteBuffer.allocate(Long.BYTES);
                    buffer2.put(recv.getData(), Long.BYTES, Long.BYTES);
                    buffer2.flip();
                    long total = buffer2.getLong();
                    System.out.println("final number is " + total + "\n");

                    fos.write(recv.getData(), Long.BYTES * 2, Config.sendSize);
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
                }
                else{
                    System.out.println("Client: no start message received");
                }
            }
            close();
            fos.close();
            System.out.println("it closed as well and stuff");
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    private static boolean receiveStart() {
        try {
            byte[] buf = new byte[Config.initSize];
            DatagramPacket recv = new DatagramPacket(buf, buf.length);
            s.receive(recv);

            if (recv.getData()[0] == 1){

            ByteBuffer buffer3 = ByteBuffer.allocate(Long.BYTES);
            buffer3.put(recv.getData(), 1, Integer.BYTES);
            buffer3.flip();
            int packetNumber = buffer3.getInt();
            System.out.println("client: packet number is " + packetNumber);

            ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES);
            buffer.put(recv.getData(), Integer.BYTES +1, Long.BYTES);
            buffer.flip();
            long fileSize = buffer.getLong();
            System.out.println("client: file size is " + fileSize);

            ByteBuffer buffer2 = ByteBuffer.allocate(Integer.BYTES);
            buffer2.put(recv.getData(),Long.BYTES + Integer.BYTES + 1,Integer.BYTES);
            buffer2.flip();
            int packetSize = buffer2.getInt();
            System.out.println("client: packet size is " + packetSize);

            String s =  new String(recv.getData(),Integer.BYTES + Long.BYTES+1,256, StandardCharsets.UTF_8);
            s = s.trim();
            System.out.println("client: string recieved is " + s);

            sendAck(packetNumber);
            }
            //TODO change this to true to actually test the damn thing
            return false;
        } catch (Exception e) {
            return false;
        }

    }

    private static void sendAck(int packetNumber) throws Exception {
        ByteBuffer buffer2 = ByteBuffer.allocate(Integer.BYTES);
        buffer2.putInt(packetNumber);
        byte[] send = new byte[Integer.BYTES +1];
        byte[] d = buffer2.array();
        System.arraycopy(d,0,send,1,d.length);
        send[0] =0;

        packet = new DatagramPacket(send, send.length, group, Config.port);
        Thread.sleep(10000);
        s.send(packet);
        System.out.println("client: sent packet " + packetNumber);

    }


}
