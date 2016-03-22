package TTP;

/**
 * Created by Shekhar on 3/22/2016.
 */
public class TTPPacket {
    private int acknowledgementNumber;
    private int sequenceNumber;
    private byte packetFlags; // 0th byte: SYN, 1st byte: ACK, 2nd byte: Data
    private Object data;

    public TTPPacket(int sequenceNumber, Object data) {
        this.sequenceNumber = sequenceNumber;
        packetFlags = 0;
        this.data = data;
    }

    public TTPPacket(int sequenceNumber) {
        this.sequenceNumber = sequenceNumber;
        packetFlags = 0;
        data = null;
    }

    public int getAcknowledgementNumber() {
        return acknowledgementNumber;
    }

    public void setAcknowledgementNumber(int ack) {
        this.acknowledgementNumber = ack;
    }

    public void setSyn() {
        packetFlags = (byte) (packetFlags | 1);
    }

    public boolean isSyn() {
        return (packetFlags & 1) == 1;
    }

    public void setAcknowledge() {
        packetFlags = (byte) (packetFlags | 2);
    }

    public boolean isAcknowledge() {
        return (packetFlags & 2) == 2;
    }

    public void setData() {
        packetFlags = (byte) (packetFlags | 4);
    }

    public boolean isData() {
        return (packetFlags & 4) == 4;
    }

    public void setFin() {
        packetFlags = (byte) (packetFlags | 8);
    }

    public boolean isFin() {
        return (packetFlags & 8) == 8;
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

}
