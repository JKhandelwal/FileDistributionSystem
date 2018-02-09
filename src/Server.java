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
            String stringIP = "Blep";
            group = InetAddress.getByName("228.5.6.7");
            s = new MulticastSocket(2345);
            s.joinGroup(group);
            serverIP = new DatagramPacket(stringIP.getBytes(), stringIP.length(), group, 2345);
            s.send(serverIP);
            System.out.println("server sent it");
        }catch (Exception e){
            System.out.println("It broke the system");
        }
//system
//
//        while(readFile != Null){
//            continue sending;
//        }
    }
}
