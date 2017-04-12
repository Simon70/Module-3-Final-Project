package utwente.ns.linklayer;

import utwente.ns.IPacket;
import utwente.ns.IReceiveListener;
import utwente.ns.config.Config;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by simon on 07.04.17.
 */
public class SimulatedLinkLayer implements ILinkLayer {
    
    private List<IReceiveListener> packetListeners;
    private InetAddress address;
    private MulticastSocket socket;
    private Thread receiver;
    private boolean closed;
    private int maxSegmentSize;
    
    public SimulatedLinkLayer(int maxSegmentSize) throws IOException {
        this.maxSegmentSize = maxSegmentSize;
        packetListeners = new ArrayList<>();
        address = InetAddress.getByName(Config.getInstance().getMulticastAddress());
        socket = new MulticastSocket(Config.getInstance().getMulticastPort());
        receiver = new Thread(this::waitForIncomingPackets);
        receiver.setDaemon(true);
        receiver.setName("LinkLayerReceiver");
        receiver.start();
    }
    
    @Override
    public void send(IPacket packet) throws IOException {
        send(packet.marshal());
    }
    
    @Override
    public void send(byte[] data) throws IOException {
        DatagramPacket sendPacket = new DatagramPacket(data, data.length, address, Config.getInstance().getMulticastPort());
        socket.send(sendPacket);
    }

    @Override
    public void addReceiveListener(IReceiveListener receiver) {
        packetListeners.add(receiver);
    }
    
    @Override
    public void removeReceiveListener(IReceiveListener receiver) {
        packetListeners.remove(receiver);
    }
    
    private void waitForIncomingPackets() {
        closed = false;
        while (!closed) {
            byte[] receivedData = new byte[maxSegmentSize];
            DatagramPacket receivedPacket = new DatagramPacket(receivedData, maxSegmentSize);
            try {
                socket.receive(receivedPacket);
                for (IReceiveListener listener : packetListeners) {
                    listener.receive(new SimulatedLinkPacket(receivedPacket));
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    
    @Override
    public int getLinkCost(String address) {
        return 1;
    }
    
    @Override
    public int getLinkCost(InetAddress address) {
        return 1;
    }
    
    @Override
    public InetAddress getLocalAddress() {
        return socket.getLocalAddress();
    }
    
    @Override
    public void close() throws IOException {
        closed = true;
    }
}
