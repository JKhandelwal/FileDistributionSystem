import java.io.FileInputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Main {

    public static void main(String[] args) {
        try {
            messAround();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
//        work();
    }
//
    private static void work() {
        Server s = new Server();
        s.start();
        Client c = new Client();
        c.start();
    }

    private static void messAround() throws InterruptedException {
//        long value = Long.MAX_VALUE;
//        System.out.println(value + " bytes");
//        value /= 1024;
//        System.out.println(value + " kilobytes");
//        value /= 1024;
//        System.out.println(value + " megabytes");
//        value /= 1024;
//        System.out.println(value + " gigabytes");
//        value /= 1024;
//        System.out.println(value + " terabytes");
//
//        System.out.println(Long.BYTES);
//
//        ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES);
//        buffer.putLong(12456);
//        byte[] a = buffer.array();
//
//        byte[] b = new byte[Long.BYTES *2];
//        System.arraycopy(a,0,b,0,Long.BYTES);
//
//        System.out.println(Arrays.toString(b));

//        String s = "";
//        for (int i =0; i < 255;i++){
//            s += "a";
//        }
//        byte[] bytes = s.getBytes();
//
//        System.out.println("length is " + bytes.length);
//        boolean  t = true;
//        boolean vIn = false;
//        byte vOut = (byte)(vIn?1:0);
//
//        System.out.println(vOut);

//        List<Integer> range = IntStream.rangeClosed(0,10)
//                .boxed().collect(Collectors.toList());
//
//
//        System.out.println(Arrays.toString(range.toArray()));

        Test t = new Test();

        t.start();

        System.out.println(t.getMap());

//        t.set(1,2);

        t.setMap(new HashMap<>());
        Thread.sleep(2400);
//        t.set(4,7);
        System.out.println(t.getMap());

        t.setMap(new HashMap<>());
        Thread.sleep(2400);
//        t.set(4,7);
        System.out.println(t.getMap());
        t.setMap(new HashMap<>());
        Thread.sleep(2400);
//        t.set(4,7);
        System.out.println(t.getMap());






    }
}
