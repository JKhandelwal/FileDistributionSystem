import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;

public class ServerControl extends Thread {

    private Socket connectionSocket;
    private InputStream is;
    private OutputStream os;
    private PrintWriter pw;
    private BufferedReader br;
    private File f;
    private int packetSize;
    private CountDownLatch latch = new CountDownLatch(1);
    private String sum;
//    private CountDownLatch exitLatch = new CountDownLatch(2);


    public BlockingQueue<ArrayList<Integer>> mainQueue;

    public ServerControl(String checksum,Socket connectionSocket, File file, int size, BlockingQueue<ArrayList<Integer>> startQueue) {
        this.connectionSocket = connectionSocket;
        this.f = file;
        this.sum = checksum;
        this.mainQueue = startQueue;
        this.packetSize = size;
        try {
            this.is = connectionSocket.getInputStream();
            this.os = connectionSocket.getOutputStream();
            br = new BufferedReader(new InputStreamReader(is));
            pw = new PrintWriter(new OutputStreamWriter(connectionSocket.getOutputStream()), true);
        } catch (IOException e) {
            System.out.println("Server - Errors Occurred:" + e.getMessage());
        }
    }

    public void sendInit() throws Exception {
        pw.println(f.getName());
        pw.flush();
        pw.println(f.length());
        pw.flush();
        pw.println(sum);
        pw.flush();
        pw.println(packetSize);
        pw.flush();
        System.out.println("Server Control - Init Sent");
    }


    public void run() {
        System.out.println("Server - new ConnectionHandler thread started .... ");

        try {

            sendInit();

            String line = br.readLine();
            if (!line.equals("receivedStart")) {
                System.out.println("Server control - protocol is broken, aborting");
                return;
            }

            while (true) {

                latch.await();
                line = br.readLine();
                System.out.println("Server Control - Re requesting " + line);
                ArrayList<Integer> list = new ArrayList<>();
                int num = Integer.parseInt(line);
                if (num != 0) {
                    for (int i = 0; i < num; i++) {
                        line = br.readLine();
                        list.add(Integer.parseInt(line));
                    }
                    mainQueue.add(list);
                    System.out.println(list.toString());
                } else {
                    assert list.size() == 0;
                    mainQueue.add(list);
                    System.out.println("Server Control - Exit Received");
                    cleanup();
                    return;
                }
//                System.out.println("List size is " + list.size());
                latch = new CountDownLatch(1);
            }
        } catch (Exception e) {
            System.out.println("Server - ConnectionHandler: Thread Connection Lost " + e.getMessage());
            pw.print("exit");
            ArrayList<Integer> list = new ArrayList<>();
            assert list.size() == 0;
            mainQueue.add(list);
            cleanup();
            return;
        }
        //send message to close;
    }

    /**
     * Cleanup method to close the thread and close the socket to the user.
     */
    private void cleanup() {
        System.out.println("Server - Connection Closed to..." + connectionSocket.getInetAddress());
        try {
            if (br != null) br.close();
            if (os != null) os.close();
            if (is != null) is.close();
            if (pw != null) pw.close();
            if (connectionSocket != null) connectionSocket.close();
        } catch (IOException ioe) {
            System.out.println("Server " + ioe.getMessage());
        }
    }


    public void latchContinue() {
        latch.countDown();
    }

    public void send(){
        pw.println("sending");
        pw.flush();
    }

    public void sendReTransmitMessage(){
        pw.println("retransmit");
        pw.flush();
    }

//    public void setExitLatch() {
//        exitLatch.countDown();
//    }
}
