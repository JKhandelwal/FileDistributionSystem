package UDP;

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
    private String buffStr;
    private BufferedReader br;
    private File f;
    private int packetSize;
    private int lowNumPacket = 0;
    private int highNumPacket = 2;
    private CountDownLatch latch = new CountDownLatch(1);
    private CountDownLatch exitLatch = new CountDownLatch(2);


    public BlockingQueue<ArrayList<Integer>> mainQueue;

    public ServerControl(Socket connectionSocket, File file, int size, BlockingQueue<ArrayList<Integer>> startQueue) {
        this.connectionSocket = connectionSocket;
        this.f = file;
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

    public void runOld() {
        try {
            //TODO connection shit
//            sendFile();
        } catch (Exception e) {

            cleanup();
        }
    }

    public void sendInit() throws Exception {
        pw.println(f.getName());
        pw.flush();
        pw.println(f.length());
        pw.flush();
        pw.println(packetSize);
        pw.flush();
        System.out.println("Server - IT SENT THE INIT");
    }


    public void run() {
        System.out.println("Server - new ConnectionHandler thread started .... ");

        try {

            sendInit();

            String line = br.readLine();
            if (!line.equals("receivedStart")) {
                System.out.println("protocol is broken, aborting");
                return;
            }

            while (exitLatch.getCount() == 2) {
                // 1
                latch.await();
                System.out.println("Low " + lowNumPacket + " high " + highNumPacket);
                pw.println("sending," + lowNumPacket + "," + highNumPacket);
                pw.flush();
                latch = new CountDownLatch(1);

                // 2
                latch.await();

                pw.println("retransmit");
                pw.flush();

                line = br.readLine();
                System.out.println("Read the number " + line);
                ArrayList<Integer> list = new ArrayList<>();
                int num = Integer.parseInt(line);
                if (num != 0) {
                    for (int i = 0; i < num; i++) {
                        line = br.readLine();
                        list.add(Integer.parseInt(line));
                    }
                } else {
                    assert list.size() == 0;
                }
                System.out.println("List size is " + list.size());
                mainQueue.add(list);
                latch = new CountDownLatch(1);
            }

            pw.println("exit,0,0");
            pw.flush();

            cleanup();
            return;

        } catch (Exception e) {
            System.out.println("Server - ConnectionHandler:run " + e.getMessage());
            pw.print("exit");
            cleanup();
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


    public void latchContinue(int lowerBound, int upperBound) {
        this.lowNumPacket = lowerBound;
        this.highNumPacket = upperBound;
        latch.countDown();
    }


    public void setExitLatch() {
        exitLatch.countDown();
    }
}
