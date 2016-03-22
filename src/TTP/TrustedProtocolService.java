package TTP;

import datatypes.Datagram;
import services.DatagramService;

import java.io.*;
import java.util.concurrent.*;

/**
 * Created by Shekhar on 3/20/2016.
 */
public class TrustedProtocolService {
    int SenderBase; //start of the sender window
    int SenderMax; //end of the sender window
    int SenderCurrent; //current frame being sent
    int ReceiverExpected; //expected next packet/frame by receiver
    int Timeout; //Time out in seconds
    //list of acks received corresponding to each client
    ConcurrentHashMap<String, ConcurrentSkipListSet<Integer>> Acks;
    DatagramService ds;


    public TrustedProtocolService(int Timeout, int WindowSize) {
        SenderBase = 0;
        SenderMax = WindowSize;
        SenderCurrent = 0;
        ReceiverExpected = 0;
        this.Timeout = Timeout;
    }

    /* Client-side method to setup connection with server */
    public void startConnection(String srcIP, short srcPort, String destIP, short destPort)
            throws IOException, ClassNotFoundException {
        final DatagramService ds = new DatagramService(srcPort, 10);
        short zero = 0;
        /* Send SYN message */
        TTPPacket SYN = new TTPPacket(1);
        SYN.setSyn();
        Datagram synDatagram = new Datagram(srcIP, destIP, srcPort, destPort, zero, zero, SYN);
        ds.sendDatagram(synDatagram);
        final Runnable receiveAcknowledgement = new Thread() {
            public void run() {
				/* Receive acknowledgement */
                Datagram ackDatagram = null;
                try {
                    ackDatagram = ds.receiveDatagram();
                } catch (Exception e) {
                    e.printStackTrace();
                    return;
                }
                TTPPacket ACK = (TTPPacket) ackDatagram.getData();
                if (ACK.isAcknowledge() && ACK.getAcknowledgementNumber() == 2)
                    System.out.println("Connection Established");
                else
                    System.out.println("Connection error with server");
            }
        };
        final ExecutorService executor = Executors.newSingleThreadExecutor();
        final Future future = executor.submit(receiveAcknowledgement);
        executor.shutdown();
        try {
            future.get(Timeout, TimeUnit.MILLISECONDS);
        } catch (Exception te) {
            System.out.println("Connection Timed Out");
        }
        if (!executor.isTerminated())
            executor.shutdownNow();
    }

    /* Server side method to accept connection from client */
    public void acceptConnection(int srcPort) throws IOException, ClassNotFoundException {
        if (ds == null)
            ds = new DatagramService(srcPort, 10);
        Datagram datagram = ds.receiveDatagram();
        System.out.println("Received datagram from " + datagram.getSrcaddr() + ":" + datagram.getSrcport() + " Data: " + datagram.getData());
        TTPPacket SYN = (TTPPacket) datagram.getData();
        if (SYN.isSyn() && SYN.getSequenceNumber() == 1) {
            TTPPacket ackPac = new TTPPacket(1);
            ackPac.setAcknowledge();
            ackPac.setAcknowledgementNumber(2);
            short zero = 0;
            Datagram ack = new Datagram(datagram.getDstaddr(), datagram.getSrcaddr(), datagram.getDstport(), datagram.getSrcport(), zero, zero, ackPac);
            ds.sendDatagram(ack);
        }
    }

    public void SendFrames(Datagram[] datagrams, int port) throws
            IOException {
        DatagramService datagramService = new DatagramService(port, 10);
        while (SenderMax <= datagrams.length) {
            while (SenderCurrent <= SenderMax) {
                datagramService.sendDatagram(datagrams[SenderCurrent++]);
            }
            String destIp = datagrams[SenderCurrent].getDstaddr();
            int maxAck = -1;
            if (Acks.containsKey(destIp))
                maxAck = Acks.get(destIp).last();
            if (maxAck < SenderBase) {
                try {
                    Thread.sleep(Timeout * 1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if (Acks.containsKey(destIp))
                    maxAck = Acks.get(destIp).last();
                if (maxAck < SenderBase) {
                    continue;
                }
            }
            SenderMax = SenderMax + (maxAck - SenderBase);
            SenderBase = maxAck;
        }
    }

    public void RecieveAcks(int port) throws IOException, ClassNotFoundException {
        DatagramService datagramService = new DatagramService(port, 10);
        while (true) {
            Datagram datagram = null;
            datagram = datagramService.receiveDatagram();
            if (datagram != null) {
                TTPPacket ttpPacket = (TTPPacket) datagram.getData();

            }
        }
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
        int countDatagrams = (int) Math.ceil((file.length()) / 1451);
        Datagram[] datagrams = new Datagram[countDatagrams];
        char[] buffer = new char[1451];
        BufferedReader bufferedReader = new BufferedReader(new FileReader
                (file.getAbsolutePath()));
        int i = 0;
        while (bufferedReader.read(buffer, 0, 512) != -1) {
            datagrams[i].setSrcaddr(srcIP);
            datagrams[i].setDstaddr(destIP);
            datagrams[i].setSrcport(srcPort);
            datagrams[i].setDstport(destPort);
            datagrams[i].setChecksum(checksum(new String(buffer).getBytes()));
            //TPPacket ttpPacket = new TTPPacket()
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
