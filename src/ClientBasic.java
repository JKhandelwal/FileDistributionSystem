import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;

public class ClientBasic {
    private static MulticastSocket s;
    private static InetAddress group;
    private static int last;
    private static int sendSize;
    private static String fName = "";
    private static HashMap<Long, byte[]> map = new HashMap<>();
    private static long total = 0;

    public static void main(String[] args) {
        try {
            group = InetAddress.getByName(Config.ip);
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
            if (receiveStart()) {
                File f = new File("/cs/scratch/jk218/" + fName);
                FileOutputStream fos = new FileOutputStream(f);
                while (true) {


                    //Gets the DataGram socket
                    byte[] buf = new byte[sendSize + Long.BYTES * 2];
                    DatagramPacket recv = new DatagramPacket(buf, buf.length);
                    s.receive(recv);

                    ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES);
                    buffer.put(recv.getData(), 0, Long.BYTES);
                    buffer.flip();
                    long curr = buffer.getLong();
//                    System.out.println("current number is received " + curr);

                    ByteBuffer buffer2 = ByteBuffer.allocate(Long.BYTES);
                    buffer2.put(recv.getData(), Long.BYTES, Long.BYTES);
                    buffer2.flip();
                    total =buffer2.getLong();
//                    System.out.println("final number is " + total + "\n");

                    if (curr == total) break;
////                        fos.write(recv.getData(), Long.BYTES * 2, last);
//                        break;
//                    } else {
//
//                    }
                    map.put(curr, recv.getData());
                }
                for (int i = 1; i < total; i++) {
//                    System.out.println(i);
                    if (map.get((long)i) != null){
                        fos.write(map.get((long) i), Long.BYTES * 2, sendSize);
                    } else {
                        System.out.println("didn't get + " + i);
                    }
                }
                System.out.println(total);
                if (map.get(total) == null){
                    System.out.println("didn't get the final packet");
                } else
                fos.write(map.get(total), Long.BYTES * 2, last);


                fos.close();
            } else {
                System.out.println("Client: no start message received");
            }
            close();

            System.out.println("it closed as well and stuff");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static boolean receiveStart() {
        try {
            byte[] buf = new byte[Config.initSize];
            DatagramPacket recv = new DatagramPacket(buf, buf.length);
            s.receive(recv);

            ByteBuffer buffer3 = ByteBuffer.allocate(Integer.BYTES);
            buffer3.put(recv.getData(), 0, Integer.BYTES);
            buffer3.flip();
            int packetNumber = buffer3.getInt();
            System.out.println("client: packet number is " + packetNumber);

            ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES);
            buffer.put(recv.getData(), Integer.BYTES, Long.BYTES);
            buffer.flip();
            long fileSize = buffer.getLong();
            System.out.println("client: file size is " + fileSize);

            ByteBuffer buffer2 = ByteBuffer.allocate(Integer.BYTES);
            buffer2.put(recv.getData(), Long.BYTES + Integer.BYTES, Integer.BYTES);
            buffer2.flip();
            int packetSize = buffer2.getInt();
            System.out.println("client: packet size is " + packetSize);

            String str = new String(recv.getData(), Integer.BYTES * 2 + Long.BYTES, 256, StandardCharsets.UTF_8);
            str = str.trim();
            System.out.println("client: string recieved is " + str);


            last = (int) (fileSize % packetSize);
            sendSize = packetSize;
            fName = str;
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
//            return false;
    }


}
