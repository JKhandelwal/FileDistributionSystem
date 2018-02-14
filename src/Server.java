import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.nio.ByteBuffer;
import java.util.Arrays;

public class Server extends Thread {
    private DatagramPacket packet;
    private static MulticastSocket s;
    private static InetAddress group;

    public void run() {
        startMutiCast();
    }

    private void startMutiCast() {
        try {

            group = InetAddress.getByName(Config.ip);
            s = new MulticastSocket(Config.port);
            s.joinGroup(group);
            GetAck g = new GetAck(s);
            g.start();
//
//            while (true) {
            Thread.sleep(1000);
//                s.send(serverIP);
//                System.out.println("server sent it");
//            }

            if (sendInit()) {


                try {
                    File file = new File(Config.filePath);
                    FileInputStream is = new FileInputStream(file);
//                System.out.println("file size is " + file.length());
                    long totalNum = (long) Math.ceil((double) file.length() / (double) Config.sendSize);
                    byte[] chunk = new byte[Config.sendSize];
                    int chunkLen = 0;
                    int currentNum = 1;

                    while ((chunkLen = is.read(chunk)) != -1) {
                        ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES);
                        buffer.putLong(currentNum++);
                        byte[] a = buffer.array();

                        ByteBuffer buffer1 = ByteBuffer.allocate(Long.BYTES);
                        buffer1.putLong(totalNum);
                        byte[] b = buffer1.array();

                        byte[] send = new byte[chunk.length + Long.BYTES * 2];

                        System.arraycopy(a, 0, send, 0, Long.BYTES);
                        System.arraycopy(b, 0, send, a.length, Long.BYTES);
                        System.arraycopy(chunk, 0, send, a.length + b.length, chunk.length);


                        packet = new DatagramPacket(send, send.length, group, Config.port);
                        s.send(packet);
//                        Thread.sleep(1);
//                        System.out.println("sent the packet length is " + send.length )
                    }
                    System.out.println("num is " + currentNum);

                    is.close();

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            } catch(Exception e){
                e.printStackTrace();
                System.out.println("It broke the system");
            }
//system
//
//        while(readFile != Null){
//            continue sending;
//        }

    }

    private boolean sendInit() throws IOException, InterruptedException {
        int packetSize = 1024*2*2*2;
//        System.out.println(packetSize);
        long fileSize = 128812;
        String str = "maybe this works?";

        ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES);
        buffer.putLong(fileSize);
        byte[] a = buffer.array();

        ByteBuffer buffer1 = ByteBuffer.allocate(Integer.BYTES);
        buffer1.putInt(packetSize);
        byte[] b = buffer1.array();

        ByteBuffer buffer2 = ByteBuffer.allocate(Integer.BYTES);
        buffer2.putInt(340);
        byte[] d = buffer2.array();

        byte[] c = str.getBytes();

        byte[] send = new byte[Config.initSize];
        send[0] =1;
        System.arraycopy(d, 0, send, 1, Integer.BYTES);

        System.arraycopy(a, 0, send, Integer.BYTES +1, Long.BYTES);
//        System.out.println(Arrays.toString(send));
        System.arraycopy(b, 0, send, a.length + Integer.BYTES +1, Integer.BYTES);
//        System.out.println(Arrays.toString(send));
        System.arraycopy(c, 0, send, a.length + b.length + Integer.BYTES +1, c.length);
//        System.out.println(Arrays.toString(send));
        packet = new DatagramPacket(send, send.length, group, Config.port);


        s.send(packet);
        System.out.println("packet sent");

        int counter = 0;
        while (counter < 5) {
            if (GetAck.messages() != null){
                return false;
            }
            Thread.sleep(1000);
            counter++;
        }
        return false;
    }
}
