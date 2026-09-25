public enum EventType {
    SECURITY_SSH, // E.g., failed login attempts, brute force attacks, unauthorized access attempts
    SYSTEM_CRASH, // E.g., kernel panic, blue screen of death, unexpected shutdowns
    NETWORK_OFFLINE, // E.g., network interface down, loss of connectivity, DNS resolution failures
    FILE_TRANSFER,
    SYSTEM_ALERT,
    SERVER_ERROR,
    UNKNOWN
}
