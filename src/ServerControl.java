import java.io.*;
import java.net.Socket;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.CountDownLatch;

public class ServerControl extends Thread{

    private Socket connectionSocket;
    private InputStream is;
    private OutputStream os;
    private PrintWriter pw;
    private String buffStr;
    private BufferedReader br;
    private File f;
    private BufferedInputStream bis;
    private int packetSize;

    public ServerControl(Socket connectionSocket,File file,int size) {
        this.connectionSocket = connectionSocket;
        this.f = file;
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

    public void run() {
        System.out.println("Server - new ConnectionHandler thread started .... ");
        try {
            //TODO connection shit
            sendFile();
        } catch (Exception e) {
            System.out.println("Server - ConnectionHandler:run " + e.getMessage());
            cleanup();
        }
    }

    private CountDownLatch latch = new CountDownLatch(1);

    public void run_me(int numberOfSections) throws InterruptedException {
//        sendInitToClient();
//        waitForInitFromClient();
        latch.await();
        for (int i =0;i < numberOfSections;i++){
            //send message to tell the client to be ready to receive
            latch = new CountDownLatch(1);
            latch.await();
            //receive the list from the client;
            //transmit the list to the main server
            latch = new CountDownLatch(1);
            latch.await();
            //send message to transmit
        }

        //send message to close;
    }

    private void sendFile() {
        try {


                pw.println(f.getName());
                pw.println(f.length());
                pw.println(packetSize);
                //Gets the name of the file the client wishes to get.

                buffStr = br.readLine();
                //ack Server
                if (buffStr.equals("receivedStart")) {
                while (true){
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
        } catch (IOException e) {
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
            if (bis != null) bis.close();
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
}
