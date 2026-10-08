import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server implements Runnable {
    private final int port;
    private final ExecutorService threadPool;
    private ServerSocket serverSocket;
    private volatile boolean isRunning;

    public Server(int port) {
        this.port = port;
        this.threadPool = Executors.newFixedThreadPool(50);
    }

    public void start() {
        if (!isRunning) {
            isRunning = true;
            Thread serverThread = new Thread(this, "Server-Main-Loop");
            serverThread.start();
        }
    }

    @Override
    public void run() {
        System.out.println("Starting server on port " + port + "...");

        try (ServerSocket socket = new ServerSocket(port)) {
            this.serverSocket = socket;
            System.out.println("Server started! Listening on port " + port + "\n");

            while(isRunning && !serverSocket.isClosed()) {
                Socket clientSocket = serverSocket.accept();

                System.out.println("[SERVER] Incoming connection channel opened.");
                System.out.println("  > Remote Endpoint: " + clientSocket.getRemoteSocketAddress());
                System.out.println();

                ClientHandler handler = new ClientHandler(clientSocket);
                threadPool.execute(handler);
            }
        }
        catch (IOException e) {
            System.out.println("[ERROR] Could not bind to port "+ port +": "+e.getMessage());
        }
        finally { stop(); }
    }

    public synchronized void stop() {
        if (!isRunning) return;

        System.out.println("Stopping server...");
        isRunning = false;

        try {
            if(serverSocket != null && !serverSocket.isClosed())
                serverSocket.close();
        }
        catch (IOException e) {
            System.out.println("[ERROR] Problem closing server socket: " + e.getMessage());
        }
        threadPool.close();
        System.out.println("Server fully stopped");
    }

    public static void main(String[] args) {
        Server myServer = new Server(6666);
        myServer.start();
    }
}
