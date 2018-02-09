import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;

public class Server extends Thread{
    private DatagramPacket serverIP;
    private static MulticastSocket s;
    private static InetAddress group;

    public void run(){
        startMutiCast();
    }

    private void startMutiCast() {
        try{

        }catch (Exception e){
            System.out.println("It broke the system");
        }


        while(readFile != Null){
            continue sending;
        }
    }
}
