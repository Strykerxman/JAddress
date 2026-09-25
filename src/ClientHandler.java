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
     * TODO: Implement a way to send data back to the client if needed.
     */
    private final Socket socket;
    private final int BUFFER_SIZE = 5;

    public ClientHandler(Socket s) {
        this.socket = s;
    }

    @Override
    public void run() {
        try {
            Server.threadCount.incrementAndGet();
            long threadId = Thread.currentThread().threadId();
            String remoteAddress = this.socket.getRemoteSocketAddress().toString();
            ServerMetrics.registerThread(threadId, remoteAddress);

            DataInputStream in = new DataInputStream(this.socket.getInputStream());
            // IO stream for receiving data
            byte[] payload = new byte[BUFFER_SIZE];
            // Allow fixed chunks of data at a time (5 bytes/read)
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();

            while (true) { // Loop until EOF, caught by EOFException
                int received = 0;
                int length = in.readInt(); // Thread becomes locked, freezes loop, waits for data

                while (received < length) {
                    int n = in.read(payload, 0, Math.min(BUFFER_SIZE, length - received)); // Payload rewritten with new data, up to 5 bytes at a time
                    if(n == -1) {
                        throw new EOFException("Client closed connection unexpectedly.");
                    }

                    buffer.write(payload, 0, n); // Write 'n' new bytes from payload to buffer
                    received += n;

                    System.out.println("[Thread-" + threadId+"]");
                    System.out.println("Received: " + received);
                    System.out.println();
                }
                String fullMessage = buffer.toString();
                System.out.println("[Thread-" + threadId+"]"+" Full message received: " + fullMessage);
                buffer.reset(); // Clear buffer for next message
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
}
