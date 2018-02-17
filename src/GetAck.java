import java.io.IOException;
import java.net.DatagramPacket;
import java.net.MulticastSocket;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.HashMap;

public class GetAck extends Thread{
    public static HashMap<String, byte[]> map = new HashMap<String, byte[]>();
    public static MulticastSocket s;

    public GetAck(MulticastSocket s){
        this.s = s;
    }

    public void run(){
        byte[] buf = new byte[Config.ackSize];
        DatagramPacket recv = new DatagramPacket(buf, buf.length);
        try {
            s.receive(recv);
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("server: here");
        if (recv.getData()[0] ==0){
            ByteBuffer buffer3 = ByteBuffer.allocate(Integer.BYTES);
            buffer3.put(recv.getData(), 1, Integer.BYTES);
            buffer3.flip();
            int num = buffer3.getInt();
            System.out.println("server: ack received for packet number " + num);
            map.put(recv.getAddress().toString(), Arrays.copyOfRange(recv.getData(), 1, recv.getData().length ));
        }
    }


    public static HashMap<String, byte[]> messages(){
        if (map.size() ==0){ return null;}
        else {
            return map;
        }
    }

}
