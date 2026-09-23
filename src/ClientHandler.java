import java.io.DataInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.net.Socket;
import java.time.LocalDateTime;

public class ClientHandler implements Runnable{
    private final Socket socket;
    private final int BUFFER_SIZE = 5;

    public ClientHandler(Socket s) {
        this.socket = s;
    }

    @Override
    public void run() {
        try {
            Server.threadCount.incrementAndGet();
            DataInputStream in = new DataInputStream(this.socket.getInputStream());
            // IO stream for receiving data
            byte[] payload = new byte[BUFFER_SIZE];
            // Allow fixed chunks of data at a time (5 bytes/read)

            while (true) { // Loop until EOF, caught by EOFException
                int received = 0;
                int length = in.readInt(); // Thread becomes locked, freezes loop, waits for data

                while (received < length) {
                    int n = in.read(payload, 0, Math.min(BUFFER_SIZE, length - received));

                    String res = new String(payload, 0, n); // take 'n' bytes from payload
                    System.out.println("[Thread-" + Thread.currentThread().threadId()+"]");
                    System.out.println("Client message received: " + res + " with n=" + n);

                    received += n;
                    System.out.println("Received: " + received);
                    System.out.println();
                }
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
            try { // Attempt closing the socket,
                System.out.println("[Thread-" + Thread.currentThread().threadId() + "] Stopping...");
                Server.threadCount.decrementAndGet();
                this.socket.close();
            } catch (IOException ignored) {}
        }
    }
}
