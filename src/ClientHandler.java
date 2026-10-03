import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.net.Socket;
import java.time.LocalDateTime;

public class ClientHandler implements Runnable {
    /**
     * ClientHandler is a Runnable class that handles incoming client connections.
     * It reads data from the client in fixed-size chunks and processes it.
     * The class also manages thread registration and unregistration for metrics tracking.
     */
    private final Socket socket;

    public ClientHandler(Socket s) {
        this.socket = s;
    }

    @Override
    public void run() {
        try {
            BinaryWireProtocol wire = initHandler();

            while (true) { // Loop until EOF, caught by EOFException
                wire.handleIncomingRequest();
            }
        }
        catch (EOFException e) { // Thread sees FIN packet
            System.out.println("[Thread-" + Thread.currentThread().threadId()+"] finished at "+ LocalDateTime.now());
            System.out.println();
        }
        catch (IOException e) {
            System.out.println("[Thread-" + Thread.currentThread().threadId() + "] Client disconnected abruptly: " + e.getMessage());
        }
        finally {
            try { // Attempt closing the socket
                System.out.println("[Thread-" + Thread.currentThread().threadId() + "] Closing connection to: " + this.socket.getRemoteSocketAddress());
                System.out.println("[Thread-" + Thread.currentThread().threadId() + "] Active connections remaining (self-included): " + ServerMetrics.getActiveConnections());
                System.out.println("[Thread-" + Thread.currentThread().threadId() + "] Stopping...");
                ServerMetrics.unregisterThread(Thread.currentThread().threadId());
                Server.threadCount.decrementAndGet();
                this.socket.close();
            } catch (IOException ignored) {}
        }
    }
    public BinaryWireProtocol initHandler() {
        Server.threadCount.incrementAndGet();
        long threadId = Thread.currentThread().threadId();
        String remoteAddress = this.socket.getRemoteSocketAddress().toString();
        ServerMetrics.registerThread(threadId, remoteAddress);

        return new BinaryWireProtocol(this.socket);
    }
}
