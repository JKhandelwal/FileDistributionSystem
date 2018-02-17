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


    private BlockingQueue<ArrayList<Integer>> mainQueue;

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
            sendFile();
        } catch (Exception e) {

            cleanup();
        }
    }

    public void sendInit() throws Exception {
        pw.println(f.getName());
        pw.println(f.length());
        pw.println(packetSize);
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
                pw.println("sending," + lowNumPacket + "," + highNumPacket);
                latch = new CountDownLatch(1);

                // 2
                latch.await();

                pw.println("retransmit");
                line = br.readLine();
                ArrayList<Integer> list = new ArrayList<>();
                if (Integer.parseInt(line) != 0) {
                    for (int i = 0; i < Integer.parseInt(line); i++) {
                        line = br.readLine();
                        list.add(Integer.parseInt(line));
                    }
                } else {
                    assert list.size() == 0;
                }
                mainQueue.add(list);
                latch = new CountDownLatch(1);
            }

            pw.println("exit");
            cleanup();
            return;

        } catch (Exception e) {
            System.out.println("Server - ConnectionHandler:run " + e.getMessage());
            pw.print("exit");
            cleanup();
        }
        //send message to close;
    }

    private void sendFile() {
        try {


            //use the secondary latch to know when to quit
//        sendInitToClient();
//        waitForInitFromClient();


            latch.await();
            //send message to tell the client to be ready to receive
            latch = new CountDownLatch(1);
            latch.await();


            //receive the list from the client;
            //if list is empty put int into the poll saying its empty
            //Insert list into blocking dequeue

            //transmit the list to the main server
            ArrayList<Integer> list = new ArrayList<>();
            String s;
            s = br.readLine();
            int number = Integer.parseInt(s);
            int count = 0;
            while (count < number) {
                s = br.readLine();
                list.add(Integer.parseInt(s));
            }

            mainQueue.add(list);
            latch = new CountDownLatch(1);
            //Wait on the main thread to tell the server to change the
            latch.await();
            //send message to transmit


            //Gets the name of the file the client wishes to get.

            buffStr = br.readLine();
            //ack Server
            if (buffStr.equals("receivedStart")) {
                while (true) {
                    try {

//                    byte [] byteArray  = new byte [(int)(fileDir).length()];
//                    //Reads the file using a buffered input stream
//                    bis = new BufferedInputStream(new FileInputStream(fileDir));
//                    bis.read(byteArray,0,byteArray.length);
//                    //Sends the length of the byte array containing the file to the client
//                    pw.println(byteArray.length);
//                    System.out.println("Transferring: " + fileName + " of length (" + byteArray.length + " bytes) to " +connectionSocket.getInetAddress());
//                    //Writes the file to the output stream.
//                    os.write(byteArray,0,byteArray.length);
                        os.flush();

                    } catch (IOException e) {
                        //If the file is not found, then a suitable error message is sent to the client
                        pw.println("File " + buffStr + " not found.");
                    }
                    //Allows the client to be able to access more files while keeping the connection alive.
                    pw.println("Do you want to access more files? Y or N");
                    buffStr = br.readLine();
                }

            }
            cleanup();
        } catch (Exception e) {
            System.out.println("Server - Errors: " + e.getMessage());
            cleanup();
        }

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
