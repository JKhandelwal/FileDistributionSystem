import java.net.DatagramPacket;
import java.net.MulticastSocket;
import java.util.concurrent.CountDownLatch;

public class ClientReceive extends Thread{
    private int packetSize;
    private CountDownLatch latch;
    private MulticastSocket s;

    public void ClientReceive(int packetSize,MulticastSocket s){
        this.packetSize = packetSize;
        latch = new CountDownLatch(1);
        this.s = s;
    }

    public void run(){
        while (true){
            try {
                if (latch.getCount() == 3) break;
                latch.await();
                byte[] buf = new byte[packetSize + Integer.BYTES];
                DatagramPacket recv = new DatagramPacket(buf, buf.length);
                s.receive(recv);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return;
    }

    public void countdown(){
        latch.countDown();
    }

    public void resetLatch(){
        latch = new CountDownLatch(1);
    }

    public void end(){
        latch = new CountDownLatch(3);
    }
}
