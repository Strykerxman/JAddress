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
            while (!socket.isClosed()) {
                String input = getUserInput();
                if (!validateInput(input)) continue;

                if(checkExitCondition(input)) {
                    System.out.println("[INFO] Closing client connection...");
                    wire.writeMessage(BinaryWireProtocol.ServerCode.CLOSE, "Client requesting disconnect");
                    handleServerReply();
                    break;
                }
                wire.writeMessage(BinaryWireProtocol.ServerCode.LOOKUP, input);
                handleServerReply();
            }
        }
        catch (Exception e) {
            System.out.println("[ERROR] Client execution failed: " + e.getMessage());
        }
        finally { stop(); }
    }
    private void stop() {
        // request / tell server to close when user enters 'close'
        // notify server, request close permission
        // wait for handshake, go here when break;
        // closes AFTER handler
        if (socket != null && !socket.isClosed()) {
            try {
                socket.close();
                System.out.println("[INFO] Socket connection closed cleanly");
            }
            catch (Exception e) {
                System.out.println("[WARN] Error closing client socket: " + e.getMessage());
            }
        }

    }
    private void handleServerReply() throws IOException {
        System.out.println("[INFO] Waiting for server response...");
        ProtocolFrame response = wire.readIncomingFrame();

        String hexCode = String.format("0x%02X", response.command().getCode());
        System.out.println("[INFO] Server Response: STATUS CODE " + hexCode +
                " (" + response.command().name() + ") | Message: " + response.payload());
    }
    private String getUserInput() {
        System.out.print("Address Search> ");
        return scanner.hasNextLine() ? scanner.nextLine() : "close";
    }
    private boolean validateInput(String input) {
        if (input == null || input.trim().isEmpty()) {
            System.out.println("[WARN] Input cannot be empty. Please enter a valid address or 'close' to exit.");
            return false;
        }
        return true;
    }
    private boolean checkExitCondition(String input) {
        return input.equalsIgnoreCase("close");
    }
    public static void main(String[] args) {
        try { // Initialize connection with socket, use protocol to handle communication on that socket
            Client client = new Client("localhost", 6666);
            client.start();
        }
        catch (Exception e) {
            System.out.println("[ERROR] Client execution failed: " + e.getMessage());
        }
    }
}
