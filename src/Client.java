import java.io.DataOutputStream;
import java.net.Socket;

public class Client {
    /**
     * A simple client that connects to a server on localhost:6666.
     * TODO: listen for incidents and implement a way to send them as byte frames to the server in real-time.
     */
    public static void main(String[] args) {
        try {
            System.out.println("[INFO] Starting client...");
            Socket client = new Socket("localhost", 6666);
            // Initialize client socket with local address
            DataOutputStream out = new DataOutputStream(client.getOutputStream());
            // Create out stream to send data

            byte[] message = "ABCDEFGHIJKLMNOPQRSTUVWXYZ".getBytes();
            System.out.println("[INFO] Sending \"" + new String(message)+"\" to server!");
            out.writeInt(message.length);
            out.write(message, 0, 3);
            out.write(message, 3, 10);
            out.write(message, 13, 13);

            byte[] hi = "Hi".getBytes();
            System.out.println("[INFO] Sending \"" + new String(hi)+"\" to server!");
            out.writeInt(hi.length);
            out.write(hi, 0, 1);
            out.write(hi, 1, 1);

            System.out.println("[INFO] Stopping...");
            client.close(); // Sends FIN packet to server on port 6666 as a XXXXX port
        }
        catch (Exception e) {
            System.out.println("Client error: " + e);
        }
    }
}
