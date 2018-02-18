import java.lang.reflect.Array;
import java.net.DatagramPacket;
import java.net.MulticastSocket;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;

public class ClientReceive extends Thread{
    private int packetSize;
    private CountDownLatch latch;
    private MulticastSocket s;
    private ConcurrentHashMap<Integer,byte[]> map;
    private ArrayList<Integer> totals;

    public ClientReceive(int packetSize, MulticastSocket s, ConcurrentHashMap<Integer,byte[]> m){
        this.packetSize = packetSize;
        latch = new CountDownLatch(1);
        this.s = s;
        this.map = m;
    }

    public void run(){
        while (true){
            try {
                if (latch.getCount() == 3) break;
                latch.await();

                byte[] buf = new byte[packetSize + Integer.BYTES];
                DatagramPacket recv = new DatagramPacket(buf, buf.length);
                s.receive(recv);
                ByteBuffer buffer = ByteBuffer.allocate(Integer.BYTES);
                buffer.put(recv.getData(),0, Integer.BYTES);
                buffer.flip();
                int num = buffer.getInt();
                map.put(num,recv.getData());
                totals.remove(num);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return;
    }

    public void countdown(ArrayList<Integer> t){
        this.totals = t;
        latch.countDown();
    }


    public ArrayList<Integer> getTotals(){
        return this.totals;
    }

    public void resetLatch(){
        latch = new CountDownLatch(1);
    }

    public void end(){
        latch = new CountDownLatch(3);
    }

}
