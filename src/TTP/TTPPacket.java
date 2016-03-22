package TTP;

/**
 * Created by Shekhar on 3/22/2016.
 */
public class TTPPacket {
    private int sequenceNumber;
    private int packetType; // 0: SYN, 1: ACK, 2: Data
    private Object data;

    public TTPPacket(int sequenceNumber, int packetType, Object data) {
        this.sequenceNumber = sequenceNumber;
        this.packetType = packetType;
        this.data = data;
    }

    public TTPPacket(int sequenceNumber) {
        this.sequenceNumber = sequenceNumber;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }

    public int getSequenceNumber() {
        return sequenceNumber;
    }

    public void setSequenceNumber(int sequenceNumber) {
        this.sequenceNumber = sequenceNumber;
    }

    public int getPacketType() {
        return packetType;
    }

    public void setPacketType(int packetType) {
        this.packetType = packetType;
    }

}
