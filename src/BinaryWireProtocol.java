import java.io.*;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public class BinaryWireProtocol {
    private Socket connection;
    private static final int BUFFER_SIZE = 5;
    private static final int MAX_FRAME_SIZE = 64 * 1024;

    public BinaryWireProtocol(Socket connection) { this.connection = connection; }

    public void writeMessage(ServerCode code, String message) throws IOException {
        if(message == null) throw new IllegalArgumentException("Message string cannot be null");
        if(this.connection.getOutputStream() == null) throw new IOException("Connection has no output stream");
        if(ServerCode.fromCode(code.getCode()) == null) throw new IllegalArgumentException("Unknown server code: "+code.getCode());

        DataOutputStream out = new DataOutputStream(this.connection.getOutputStream());
        byte[] messageBytes = message.getBytes(StandardCharsets.UTF_8);

        // Stream writes these sequentially in order automatically
        out.writeByte(code.getCode());       // 1 byte for the enum code
        out.writeInt(messageBytes.length);   // 4 bytes for length prefix
        out.write(messageBytes);             // Message body payload
        out.flush();
    }

    // 1. USE THIS ON THE SERVER SIDE: Reads a request, processes it, and REQUIRES a response
    public void handleIncomingRequest() throws IOException {
        DataInputStream in = new DataInputStream(this.connection.getInputStream());

        int code = in.readByte();
        int length = in.readInt();

        if(length <= 0 || length > MAX_FRAME_SIZE) {
            throw new IOException("Invalid message length framework: " + length);
        }

        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        byte[] payload = new byte[BUFFER_SIZE];
        int received = 0;
        while (received < length) {
            int n = in.read(payload, 0, Math.min(BUFFER_SIZE, length - received));
            if(n == -1) throw new EOFException("Client closed connection cleanly.");
            buffer.write(payload, 0, n);
            received += n;
        }

        String finalPayload = buffer.toString(StandardCharsets.UTF_8);
        System.out.println("[SERVER LOG] Received Code: 0x0" + code +" ("+ ServerCode.fromCode(code).name()+")" + " | Payload: " + finalPayload);

        // Force an explicit reply frame down the pipe
        if (code == ServerCode.LOOKUP.getCode()) {
            // Future business logic goes here:
            writeMessage(ServerCode.OK, "Address verified successfully.");
        } else {
            writeMessage(ServerCode.OK, "Command processed.");
        }
    }

    // 2. USE THIS ON THE CLIENT SIDE: Only reads the server's reply and stops (No loop-back echo)
    public void receiveServerResponse() throws IOException {
        DataInputStream in = new DataInputStream(this.connection.getInputStream());

        int statusCode = in.readByte();
        int length = in.readInt();

        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        byte[] payload = new byte[BUFFER_SIZE];
        int received = 0;
        while (received < length) {
            int n = in.read(payload, 0, Math.min(BUFFER_SIZE, length - received));
            if(n == -1) throw new EOFException("Server disconnected.");
            buffer.write(payload, 0, n);
            received += n;
        }

        String serverReply = buffer.toString(StandardCharsets.UTF_8);
        System.out.println("[SERVER RESPONSE] Status Code: 0x0" + statusCode + " ("+ ServerCode.fromCode(statusCode).name()+")" + " | Message: " + serverReply);
    }

    public enum ServerCode {
        OK(0x00),
        LOOKUP(0x01),
        INSERT(0x02),
        ERROR(0x03),
        UNKNOWN(0xFF);

        private final int code;
        ServerCode(int code) {
            this.code = code;
        }

        public int getCode() {
            return this.code;
        }
        public static ServerCode fromCode(int code) {
            for(ServerCode r : values()) {
                if(code == r.code) return r;
            }
            return null;
        }
    }
}
