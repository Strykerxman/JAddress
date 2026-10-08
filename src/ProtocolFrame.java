/**
 * An immutable data carrier representing a single decoded wire protocol packet.
 */
public record ProtocolFrame(BinaryWireProtocol.ServerCode command, String payload) {}
