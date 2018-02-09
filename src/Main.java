public class Main {

    public static void main(String[] args) {
        // write your code here
        Server s = new Server();
        s.start();
        ClientMultiCast c = new ClientMultiCast();
        c.start();
    }
}
