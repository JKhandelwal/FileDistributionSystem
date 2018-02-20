package UDP;

import java.lang.reflect.Array;
import java.net.DatagramPacket;
import java.net.MulticastSocket;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;

public class ClientReceive extends Thread {
    private int packetSize;
    private CountDownLatch latch;
    private MulticastSocket s;
    private ConcurrentHashMap<Integer, byte[]> map;
    private ArrayList<Integer> totals;
    private ConcurrentLinkedQueue<Integer> queue = new ConcurrentLinkedQueue<>();

    public ClientReceive(int packetSize, MulticastSocket s, ConcurrentHashMap<Integer, byte[]> m,ConcurrentLinkedQueue<Integer> queue) {
        this.packetSize = packetSize;
        System.out.println("Client - sets the packet size to be " + packetSize);
        latch = new CountDownLatch(1);
        this.s = s;
        this.map = m;
        this.queue = queue;
    }

    public void run() {
        System.out.println("Client - Started the receiving thread");
        while (true) {
            long time1 = System.nanoTime();
            try {
                if (latch.getCount() == 3) break;
                    long time2 = System.nanoTime();

                    byte[] buf = new byte[packetSize + Integer.BYTES];
                    DatagramPacket recv = new DatagramPacket(buf, buf.length);
//                System.out.println("client - Waiting for the packet");
                    long time3 = System.nanoTime();

                    s.receive(recv);
                    long time4 = System.nanoTime();

//                System.out.println("received packet " + recv.getAddress());
//                System.out.println(Arrays.toString(recv.getData()));
                    long time5 = System.nanoTime();

                    ByteBuffer buffer = ByteBuffer.allocate(Integer.BYTES);
                    buffer.put(recv.getData(), 0, Integer.BYTES);
                    buffer.flip();
                    int num = buffer.getInt();
                    long time6 = System.nanoTime();

//                System.out.println("client - " + num);
                    if (queue.contains(num)) {
//                        System.out.println("client :" + num);
                        map.put(num, recv.getData());
                        queue.remove(num);
                    }
                    long time7 = System.nanoTime();

//
//                System.out.println("long1 " + (time2 - time1));
//                System.out.println("long1 " + (time3 - time2));
//                System.out.println("long1 " + (time4 - time3));
//                System.out.println("long1 " + (time5 - time4));
//                System.out.println("long1 " + (time6 - time5));
//                System.out.println("long1 " + (time7 - time6));
                

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return;
    }

    public void countdown() {
//        this.totals = t;
//        latch = new CountDownLatch(2);
    }


    public ArrayList<Integer> getTotals() {

        synchronized (this.totals){return this.totals;}
    }

    public void resetLatch() {
        latch = new CountDownLatch(1);
    }

    public void end() {
        latch = new CountDownLatch(3);
    }

}
