import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ServerMetrics {
    private static final Map<Long, ConnectionInfo> ACTIVE_CONNECTIONS = new ConcurrentHashMap<>();

    public static void registerThread(Long threadId, String remoteAddress) {
        ACTIVE_CONNECTIONS.put(threadId, new ConnectionInfo(remoteAddress, Instant.now()));
        System.out.println("[METRICS] Active Agents Connected: " + ACTIVE_CONNECTIONS.size());
    }

    public static void unregisterThread(Long threadId) {
        ACTIVE_CONNECTIONS.remove(threadId);
        System.out.println("[METRICS] Agent disconnected. Remaining: " + ACTIVE_CONNECTIONS.size());
    }

    public static String getActiveConnections() {
        StringBuilder sb = new StringBuilder();
        sb.append("[METRICS] Active Connections:\n");
        for (Map.Entry<Long, ConnectionInfo> entry : ACTIVE_CONNECTIONS.entrySet()) {
            sb.append("Thread ID: ").append(entry.getKey())
                    .append(", Remote Address: ").append(entry.getValue().remoteAddress())
                    .append(", Connected At: ").append(entry.getValue().connectedAt())
                    .append("\n");
        }
        return sb.toString();
    }

    private record ConnectionInfo(String remoteAddress, Instant connectedAt) {}
}
