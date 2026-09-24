import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.concurrent.atomic.AtomicInteger;

public class Server {
    /**
     * A simple server that listens for incoming connections on port 6666.
     * It will accept connections from clients for reading data like SSH logs and other information.
     * It spawns a new thread for each client connection to handle communication.
     * The server runs indefinitely until manually stopped (e.g., Ctrl+C).
     */
    public final static AtomicInteger threadCount = new AtomicInteger(0);

    public static void main(String[] args) {
        System.out.println("Starting server on port 6666...");

        try {
            ServerSocket serverSocket = new ServerSocket(6666); // Initialize new listener on port: 6666
            System.out.println("Server started! Listening on port 6666.");

            while (true) { // Continuous listening (force close with Ctrl+C)
                Socket someClient = serverSocket.accept(); // Accept incoming connection
                System.out.println("Incoming: "+someClient.getRemoteSocketAddress());
                System.out.println("Client connecting to: "+someClient.getLocalSocketAddress());
                System.out.println();

                new Thread(new ClientHandler(someClient)).start();
                // Pass client onto a new thread so server can run multiple client requests simultaneously
                // q: how can i detect when a client disconnects? a: catch EOFException in ClientHandler
                // what if i already do? a: then you can unregister the thread in ServerMetrics when EOFException is caught
                // and i already do that so how can i let the server know or make the server print that update?
            }
        }
        catch (Exception e) { // Catch errors while server is running
            System.out.println("Server Error: " + e);
        }
    }
}
