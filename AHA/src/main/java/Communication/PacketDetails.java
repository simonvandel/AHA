package Communication;

import java.time.Instant;

public class PacketDetails {
    public Instant timeReceived;
    public byte[] content;
    public String deviceAddress;

    public PacketDetails(Instant timeReceived, byte[] content, String deviceAddress) {
        this.timeReceived = timeReceived;
        this.content = content;
        this.deviceAddress = deviceAddress;
    }
}
