package UDP;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.concurrent.CountDownLatch;

/**
 * Created by jalajkhandelwal on 18/02/2018.
 */
public class ClientWriteFile extends Thread{
    public FileOutputStream fos;
    public CountDownLatch latch = new CountDownLatch(1);
    public HashMap<Integer,byte[]> map = new HashMap<>();
    private int high;
    private int low;
    private int size;

    public ClientWriteFile(FileOutputStream fos,int packetSize){
        this.fos = fos;
        this.size = packetSize;
    }

    public void run(){
        while (true){

            if (latch.getCount() ==3) break;
            try {
                latch.await();
                System.out.println(low);
                for (int i =low;i <= high;i++){
                    if (map.get(i) ==null) {
//                        System.out.println("yeah it broke " + i);

                    } else fos.write(map.get(i),Integer.BYTES,size);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }

        return;
    }

    public void reset(){
        latch = new CountDownLatch(1);
    }

    public void countDown(HashMap<Integer,byte[]> m,int low,int high) {
        this.map = m;
        this.low = low;
        this.high = high;
        latch.countDown();
    }

    public void end(){
        latch = new CountDownLatch(3);
    }
}
