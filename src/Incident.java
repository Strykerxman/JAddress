import java.time.Instant;

public record Incident(
   String ticketId, // E.g. "INC-42"
   EventType eventType,
   String componentId, // E.g. "mtl-st-denis-router"
   double metricValue, // E.g. Cisco Link Severity 3 (Error), CPU Usage 85% (Warning), Disk Space 95% (Critical)
   String rawPayload, // E.g. "000046: %LINK-3-UPDOWN: Interface GigabitEthernet0/1, changed state to down"
   Instant capturedAt // Not necessarily the time of the event itself
) {}
// Record because stores immutable and wanted data.

// Incident routerFailure = new Incident(
//        "INC-42",
//        InfrastructureEventType.NETWORK_OFFLINE,
//        "mtl-st-denis-router",
//        3.0, // Cisco Link Severity 3
//        "000046: %LINK-3-UPDOWN: Interface GigabitEthernet0/1, changed state to down",
//        Instant.now()
//)


