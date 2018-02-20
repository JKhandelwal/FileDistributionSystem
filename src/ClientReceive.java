import java.net.DatagramPacket;
import java.net.MulticastSocket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;

public class ClientReceive extends Thread {
    private int packetSize;
    private CountDownLatch latch;
    private MulticastSocket s;
    private ConcurrentHashMap<Integer, byte[]> map;
    private ConcurrentLinkedQueue<Integer> mainQueue = new ConcurrentLinkedQueue<>();
    private ConcurrentLinkedQueue<Integer> writeQueue = new ConcurrentLinkedQueue<>();

    public ClientReceive(int packetSize, MulticastSocket s, ConcurrentHashMap<Integer, byte[]> m, ConcurrentLinkedQueue<Integer> queue, ConcurrentLinkedQueue<Integer> writeQueue) {
        this.packetSize = packetSize;
        System.out.println("Client - sets the packet size to be " + packetSize);
        latch = new CountDownLatch(1);
        this.s = s;
        this.map = m;
        this.mainQueue = queue;
        this.writeQueue = writeQueue;
    }

    public void run() {
        try {
            s.setSoTimeout(5000);

            System.out.println("Client - Started the receiving thread");
            while (true) {
                long time1 = System.nanoTime();
                try {
//                if (latch.getCount() == 3) break;
                    long time2 = System.nanoTime();

                    byte[] buf = new byte[packetSize + Integer.BYTES];
                    DatagramPacket recv = new DatagramPacket(buf, buf.length);
                    long time3 = System.nanoTime();

                    s.receive(recv);
                    long time4 = System.nanoTime();
                    long time5 = System.nanoTime();

                    ByteBuffer buffer = ByteBuffer.allocate(Integer.BYTES);
                    buffer.put(recv.getData(), 0, Integer.BYTES);
                    buffer.flip();
                    int num = buffer.getInt();
                    long time6 = System.nanoTime();

                    if (mainQueue.contains(num)) {
                        map.put(num, recv.getData());
                        assert map.get(num) != null;
                        writeQueue.add(num);
                        mainQueue.remove(num);
                    }
                    long time7 = System.nanoTime();
                } catch (Exception e) {
//                    e.printStackTrace();
                    System.out.println("Client Receive Thread returns");
                    return;
                }
            }
        } catch (Exception e) {
//            e.printStackTrace();
            try {
                s.setSoTimeout(0);
            } catch (SocketException e1) {
                e1.printStackTrace();
            }
            System.out.println("Client Receive Thread returns");
            return;
        }
    }

//    public void end() {
//        latch = new CountDownLatch(3);
//    }

}
