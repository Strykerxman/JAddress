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
    private final BinaryWireProtocol wire;
    private long threadId;
    private final String remoteAddress;

    public ClientHandler(Socket socket) throws IOException {
        this.socket = socket;
        this.wire = new BinaryWireProtocol(socket);
        this.remoteAddress = socket.getRemoteSocketAddress().toString();
    }

    @Override
    public void run() {
        this.threadId = Thread.currentThread().threadId();
        ServerMetrics.registerThread(threadId, remoteAddress);
        try {
            while (!socket.isClosed()) { // Loop until EOFException
                ProtocolFrame frame = wire.readIncomingFrame();
                switch (frame.command()) {
                    case LOOKUP -> wire.writeMessage(BinaryWireProtocol.ServerCode.OK, "Address verified successfully");
                    case CLOSE -> {
                        System.out.println("[INFO] Thread-" + threadId + " processing CLOSE request. Acknowledging...");
                        wire.writeMessage(BinaryWireProtocol.ServerCode.CLOSE, "Server acknowledging close handshake");
                        System.out.println("[INFO] Thread-" + threadId + " waiting for client to drop hardware link...");
                        // When the client closes its socket, this read will throw an EOFException
                        wire.readIncomingFrame();
                    }
                    default -> wire.writeMessage(BinaryWireProtocol.ServerCode.OK, "Command processed successfully");
                }
            }
        }
        catch (EOFException e) { // Thread sees FIN packet
            System.out.println("[INFO] Thread-" + threadId + " finished cleanly via handshake at "+ LocalDateTime.now());
        }
        catch (IOException e) {
            System.out.println("[WARN] Thread-" + threadId + " Client disconnected abruptly: " + e.getMessage());
        }
        finally {
            ServerMetrics.unregisterThread(threadId);
            try {
                this.socket.close();
            } catch (IOException ignored) {}
        }
    }
}
