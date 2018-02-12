import java.io.File;
import java.io.FileInputStream;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.nio.ByteBuffer;

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
//            serverIP = new DatagramPacket(stringIP.getBytes(), stringIP.length(), group, 2345);
//            while (true) {
            Thread.sleep(1000);
//                s.send(serverIP);
//                System.out.println("server sent it");
//            }

            try{
                File file = new File("Files/pg44823.txt");
                FileInputStream is = new FileInputStream(file);
//                System.out.println("file size is " + file.length());
                long totalNum = (long)Math.ceil((double)file.length() / (double)Config.sendSize);
                byte[] chunk = new byte[Config.sendSize];
                int chunkLen = 0;
                int num = 0;

                while ((chunkLen = is.read(chunk)) != -1) {
                        ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES);
                        buffer.putLong(num);
                        byte[] a = buffer.array();

                        ByteBuffer buffer1 = ByteBuffer.allocate(Long.BYTES);
                        buffer1.putLong(totalNum);
                        byte[] b = buffer1.array();

                        byte[] send  = new byte[chunk.length + Long.BYTES*2];
                        System.arraycopy(chunk, 0, send, 0, chunk.length);
                        System.arraycopy(a, 0, send, chunk.length, Long.BYTES);
                        System.arraycopy(b, 0, send, chunk.length + a.length, Long.BYTES);
                        packet = new DatagramPacket(send,send.length,group,Config.port);
                        s.send(packet);

//                        System.out.println("sent the packet length is " + send.length )
                    num ++;
                    System.out.println("num is " + num);
                }
                System.out.println("num is " + num);

                is.close();

            } catch( Exception e){
                e.printStackTrace();
            }

        } catch (Exception e) {
            System.out.println("It broke the system");
        }
//system
//
//        while(readFile != Null){
//            continue sending;
//        }
    }
}
