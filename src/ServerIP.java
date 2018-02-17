import java.io.IOException;
import java.net.DatagramPacket;
import java.net.MulticastSocket;
import java.util.concurrent.CountDownLatch;

public class ServerIP extends Thread{

    private CountDownLatch c;
    private MulticastSocket m;
    private DatagramPacket p;

    public ServerIP(MulticastSocket s,DatagramPacket p){
         this.c = new CountDownLatch(1);
         this.m = s;
         this.p = p;
    }


    public void run(){
        c.countDown();
        try {
            while(true) {
                c.await();
                System.out.println("sending");
                m.send(p);
            }

        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void countUp(){
        c = new CountDownLatch(1);
    }
}
