import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {
    private static final int PORT = 6666;

    public static void main(String[] args) {
        System.out.println("Starting server on port " + PORT + "...");

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Server started! Listening on port " + PORT + ".\n");

            while (!serverSocket.isClosed()) {
                Socket clientSocket = serverSocket.accept();

                System.out.println("[SERVER] Incoming connection channel opened.");
                System.out.println("  > Remote Endpoint: " + clientSocket.getRemoteSocketAddress());
                System.out.println();

                ClientHandler handler = new ClientHandler(clientSocket);
                Thread workerThread = new Thread(handler);
                workerThread.setName("ClientHandler-" + clientSocket.getRemoteSocketAddress());
                workerThread.start();
            }
        }
        catch (IOException e) {
            System.out.println("[CRITICAL] Server execution encountered an error: " + e.getMessage());
        }
    }
}
