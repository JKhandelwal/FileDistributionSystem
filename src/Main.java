import java.io.FileInputStream;
import java.nio.ByteBuffer;

public class Main {

    public static void main(String[] args) {
//        messAround();
        Server s = new Server();
        s.start();
        Client c = new Client();
        c.start();
    }

    private static void messAround() {
        long value = Long.MAX_VALUE;
        System.out.println(value + " bytes");
        value /= 1024;
        System.out.println(value + " kilobytes");
        value /= 1024;
        System.out.println(value + " megabytes");
        value /= 1024;
        System.out.println(value + " gigabytes");
        value /= 1024;
        System.out.println(value + " terabytes");

        System.out.println(Long.BYTES);

        ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES);
        buffer.putLong(value);
        byte[] a = buffer.array();
        ByteBuffer buffer1 = ByteBuffer.allocate(Long.BYTES);

        buffer1.put(a);
        buffer1.flip();
        System.out.println(buffer1.getLong());

    }
}
