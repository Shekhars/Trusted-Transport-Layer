package TTP;

import datatypes.Datagram;

import java.io.*;

/**
 * Created by Shekhar on 3/20/2016.
 */
public class TrustedProtocolService {
    int SenderBase; //start of the sender window
    int SenderMax; //end of the sender window
    int SenderCurrent; //current frame being sent
    int ReceiverExpected; //expected next packet/frame by receiver
    int WindowSize; //Window size of Go-Back-N arq
    int Timeout; //Time out in seconds


    public TrustedProtocolService(int WindowSize, int Timeout) {
        SenderBase = 0;
        SenderMax = WindowSize;
        SenderCurrent = 0;
        ReceiverExpected = 0;
        this.WindowSize = WindowSize;
        this.Timeout = Timeout;
    }

    public void SendFrames(Datagram[] datagrams) {
        while (SenderCurrent <= SenderMax) {

        }
    }

    public Object[] RecieveFrames() {

        return new Object[0];
    }

    /**
     * Converts a file to send into chunks of TTP Packets with sequence
     * number initialize
     *
     * @param file to send
     * @return Array of TTP Packets
     */
    public Datagram[] Chunkify(File file, String srcIP, String destIP,
                               short srcPort, short destPort) throws IOException {
        int countDatagrams = (int) Math.ceil((file.length()) / 1452);
        Datagram[] datagrams = new Datagram[countDatagrams];
        char[] buffer = new char[1452];
        BufferedReader bufferedReader = new BufferedReader(new FileReader
                (file.getAbsolutePath()));
        int i = 0;
        while (bufferedReader.read(buffer, 0, 512) != -1) {
            datagrams[i].setSrcaddr(srcIP);
            datagrams[i].setDstaddr(destIP);
            datagrams[i].setSrcport(srcPort);
            datagrams[i].setDstport(destPort);
            datagrams[i].setChecksum(checksum(new String(buffer).getBytes()));

        }
        return datagrams;
    }

    /**
     * Returns the checksum of data
     *
     * @param buffer chunk of data
     * @return checksum
     */
    public short checksum(byte[] buffer) {
        int length = buffer.length;
        int i = 0;

        long sum = 0;
        long data;

        // Handle all pairs
        while (length > 1) {
            data = (((buffer[i] << 8) & 0xFF00) | ((buffer[i + 1]) & 0xFF));
            sum += data;
            // 1's complement carry bit correction in 16-bits (detecting sign extension)
            if ((sum & 0xFFFF0000) > 0) {
                sum = sum & 0xFFFF;
                sum += 1;
            }

            i += 2;
            length -= 2;
        }

        // Handle remaining byte in odd length buffers
        if (length > 0) {
            sum += (buffer[i] << 8 & 0xFF00);
            // 1's complement carry bit correction in 16-bits (detecting sign extension)
            if ((sum & 0xFFFF0000) > 0) {
                sum = sum & 0xFFFF;
                sum += 1;
            }
        }

        // Final 1's complement value correction to 16-bits
        sum = ~sum;
        sum = sum & 0xFFFF;
        return (short) sum;
    }

    /**
     * Verifies checksum of data packet for integrity check
     *
     * @param datagram packet to check integrity
     * @return
     */
    public boolean verifyChecksum(Datagram datagram) {
        Object data = datagram.getData();
        byte[] origData = null;
        try {
            origData = convertToBytes(data);
        } catch (IOException e) {
            e.printStackTrace();
        }
        short checksum = checksum(origData);
        return checksum == datagram.getChecksum();
    }

    /**
     * Private method to convert object to byte array. Useful for checksum
     * calculation
     *
     * @param object
     * @return byte array of object
     * @throws IOException
     */
    private byte[] convertToBytes(Object object) throws IOException {
        try (ByteArrayOutputStream byteArrayOutputStream = new
                ByteArrayOutputStream();
             ObjectOutput out = new ObjectOutputStream(byteArrayOutputStream)) {
            out.writeObject(object);
            return byteArrayOutputStream.toByteArray();
        }
    }

}
