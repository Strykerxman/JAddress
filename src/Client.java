import java.io.DataOutputStream;
import java.net.Socket;

public class Client {
    public static void main(String[] args) {
        try {
            System.out.println("Starting client...");
            Socket client = new Socket("localhost", 6666);
            // Initialize client socket with local address
            DataOutputStream out = new DataOutputStream(client.getOutputStream());
            // Create out stream to send data

            byte[] message = "ABCDEFGHIJKLMNOPQRSTUVWXYZ".getBytes();
            System.out.println("Sending \"" + new String(message)+"\" to server!");
            out.writeInt(message.length);
            out.write(message, 0, 3);
            out.write(message, 3, 10);
            out.write(message, 13, 13);
            Thread.sleep(10000);

            byte[] hi = "Hi".getBytes();
            System.out.println("Sending \"" + new String(hi)+"\" to server!");
            out.writeInt(hi.length);
            out.write(hi, 0, 1);
            out.write(hi, 1, 1);

            System.out.println("Stopping...");
            client.close(); // Sends FIN packet to server on port 6666 as a XXXXX port
        }
        catch (Exception e) {
            System.out.println("Client error: " + e);
        }
    }
}
