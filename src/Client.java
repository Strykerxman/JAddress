import java.io.IOException;
import java.net.Socket;
import java.util.Scanner;

public class Client {
    /**
     * A simple client that connects to a server on a host:port.
     * Allows for sending strings through console input to the server for processing.
     * The client will continue to prompt for input until the user types 'close' to exit
     **/
    private final Socket socket;
    private final BinaryWireProtocol wire;
    private final Scanner scanner;

    public Client(String host, int port) throws IOException {
        System.out.println("[INFO] Attempting to connect to server on " + host + ":" + port + "...");
        this.socket = new Socket(host, port);
        this.wire = new BinaryWireProtocol(socket);
        this.scanner = new Scanner(System.in);
        System.out.println("[INFO] Connected!");
        System.out.println("--------------------------------------------------");
        System.out.println("Commands: Type any address to look it up, or type 'close' to exit.");
        System.out.println("--------------------------------------------------");
    }

    public void start() {
        try {
            while (true) {
                String input = getUserInput();
                validateInput(input);

                if(checkExitCondition(input)) {
                    System.out.println("[INFO] Closing client connection...");
                    break;
                }
                sendMessage(input);
                listenServerResponse();
            }
        }
        catch (Exception e) {
            System.out.println("[ERROR] Client execution failed: " + e.getMessage());
        }
        finally { teardown(); }
    }
    private void teardown() {
        if (scanner != null) scanner.close();

        if (socket != null && !socket.isClosed()) {
            try {
                socket.close();
                System.out.println("[INFO] Socket connection closed cleanly.");
            } catch (Exception ignored) {}
        }
    }
    private void sendMessage(String message) throws IOException {
        wire.writeMessage(BinaryWireProtocol.ServerCode.LOOKUP, message);
    }
    private void listenServerResponse() throws IOException {
        System.out.println("[INFO] Waiting for server response...");
        wire.receiveServerResponse();
        System.out.println();
    }
    private String getUserInput() {
        System.out.println("Address Search> ");
        return scanner.nextLine();
    }
    private void validateInput(String input) {
        if (input.trim().isEmpty()) {
            throw new IllegalArgumentException("Input cannot be empty.");
        }
    }
    private boolean checkExitCondition(String input) {
        return input.equalsIgnoreCase("close");
    }
    public static void main(String[] args) {
        try { // Initialize connection with socket, use protocol to handle communication on that socket
            Client client = new Client("localhost", 6666);
            client.start();
        } catch (Exception e) {
            System.out.println("[ERROR] Client execution failed: " + e.getMessage());
        }
    }
}
