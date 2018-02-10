public class Main {

    public static void main(String[] args) {
        // write your code here
//        messAround();
        Server s = new Server();
        s.start();
        Client c = new Client();
        c.start();
    }

    private static void messAround() {
        int a =3;
//        a.getBytes();
        System.out.println();
    }
}
