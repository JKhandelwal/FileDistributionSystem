import java.io.File;
import java.io.FileInputStream;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;

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
//            Thread.sleep(1000);
//                s.send(serverIP);
//                System.out.println("server sent it");
//            }

            try{
                File file = new File("Files/pg44823.txt");
                FileInputStream is = new FileInputStream(file);
                System.out.println("file size is " + file.length());
                System.out.println("number should be " + ((int)Math.ceil((double)file.length() / (double)Config.sendSize)));
                byte[] chunk = new byte[Config.sendSize];
                int chunkLen = 0;
                int num = 0;
                while ((chunkLen = is.read(chunk)) != -1) {
                    if (num == 0){
                      byte[] send  = new byte[chunk.length + Long.BYTES];

                    }
//                    packet = new DatagramPacket(chunk,1024,group,Config.port);
//                    s.send(packet);
                    num ++;
                }
                System.out.println("num is " + num);

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
