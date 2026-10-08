import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class BinaryWireProtocol {
    private final Socket connection;
    private final DataOutputStream out;
    private final DataInputStream in;
    private static final int MAX_FRAME_SIZE = 64 * 1024;

    private static final int BUFFER_SIZE = 5;
    private final byte[] reusableChunkBuffer = new byte[BUFFER_SIZE];
    private final byte[] reusableLengthBuffer = new byte[4];
    private final byte[] assemblyBuffer = new byte[MAX_FRAME_SIZE];

    public BinaryWireProtocol(Socket connection) throws IOException {
        this.connection = connection;
        this.out = new DataOutputStream(connection.getOutputStream());
        this.in = new DataInputStream(connection.getInputStream());
    }

    public synchronized void writeMessage(ServerCode code, String message) throws IOException {
        if(message == null) throw new IllegalArgumentException("Message string cannot be null");
        if(this.connection.getOutputStream() == null) throw new IOException("Connection has no output stream");
        if(ServerCode.fromCode(code.getCode()) == null) throw new IllegalArgumentException("Unknown server code: "+code.getCode());

        byte[] messageBytes = message.getBytes(StandardCharsets.UTF_8);

        // Stream writes these sequentially and automatically
        out.writeByte(code.getCode());       // 1 byte for the enum code
        out.writeInt(messageBytes.length);   // 4 bytes for length prefix
        out.write(messageBytes);             // Message body payload
        out.flush();
    }

    public ProtocolFrame readIncomingFrame() throws IOException {
        /**
         * Server-side command, assembles client request from bytes and returns a rich object type
         * */
        int code = in.readByte();
        int length = in.readInt();

        if(length < 0 || length > MAX_FRAME_SIZE) {
            throw new IOException("Invalid message length framework: " + length);
        }

        int received = 0;
        while (received < length) {
            int n = in.read(reusableChunkBuffer, 0, Math.min(BUFFER_SIZE, length - received));
            if(n == -1) throw new EOFException("Client closed connection cleanly.");
            System.arraycopy(reusableChunkBuffer, 0, assemblyBuffer, received, n);
            received += n;
        }

        String finalPayload = new String(assemblyBuffer, 0, length, StandardCharsets.UTF_8);
        ServerCode command = ServerCode.fromCode(code);
        return new ProtocolFrame(command, finalPayload);
    }

    public enum ServerCode {
        OK(0x00),
        LOOKUP(0x01),
        INSERT(0x02),
        ERROR(0x03),
        CLOSE(0x04),
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
            return UNKNOWN;
        }
    }
}
