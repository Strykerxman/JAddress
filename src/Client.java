import java.net.Socket;
import java.util.Scanner;


public class Client {
    /**
     * A simple client that connects to a server on localhost:6666.
     * Allows for sending strings through console input to the server for processing.
     * The client will continue to prompt for input until the user types 'close' to exit
     */
    public static void main(String[] args) {
        Socket socket = null;

        try (Scanner sc = new Scanner(System.in)) {
            System.out.println("[INFO] Connecting to Address Intelligence Server on port 6666...");
            socket = new Socket("localhost", 6666);
            BinaryWireProtocol wire = new BinaryWireProtocol(socket);
            System.out.println("[SUCCESS] Connected!");
            System.out.println("--------------------------------------------------");
            System.out.println("Commands: Type any address to look it up, or type 'close' to exit.");
            System.out.println("--------------------------------------------------");

            while (true) {
                System.out.println("Address Search> ");
                String input = sc.nextLine();

                if (input.trim().isEmpty()) {
                    System.out.println("[INFO] No input provided. Please enter a valid address or 'close' to exit.");
                    continue;
                }
                if (input.equalsIgnoreCase("close")) {
                    System.out.println("[INFO] Closing connection...");
                    break;
                }

                System.out.println("[INFO] Sending request for address: " + input);
                wire.writeMessage(BinaryWireProtocol.ServerCode.LOOKUP, input);

                System.out.println("[INFO] Waiting for server response...");
                wire.receiveServerResponse();
                System.out.println();
            }
        } catch (Exception e) {
            System.out.println("[ERROR] Client execution failed: " + e.getMessage());
        } finally {
            if (socket != null && !socket.isClosed()) {
                try {
                    socket.close();
                    System.out.println("[INFO] Socket connection closed cleanly.");
                } catch (Exception ignored) {
                }
            }
        }
    }
}
