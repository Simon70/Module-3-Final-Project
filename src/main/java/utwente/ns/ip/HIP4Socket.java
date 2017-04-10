package utwente.ns.ip;

import utwente.ns.IPacket;
import utwente.ns.IReceiveListener;

import java.util.ArrayList;
import java.util.List;

/**
 * @author rhbvkleef
 *         Created on 4/10/17
 */
public class HIP4Socket implements IReceiveListener {
    public List<IReceiveListener> listeners = new ArrayList<>();
    public short dstPort;
    private HIP4Layer ipLayer;

    // Deliberately package-private
    HIP4Socket(HIP4Layer ipLayer, short dstPort) {
        this.dstPort = dstPort;
        this.ipLayer = ipLayer;
    }

    @Override
    public void receive(IPacket packet) {
        if (!(packet instanceof HIP4Packet)) {
            return; // TODO: Throw all panic stuffs
        }

        if (((HIP4Packet) packet).getDstPort() != this.dstPort) {
            return; // Please don't panic here. (Thanks)
        }

        // TODO: Ensure thread-safety in case of multiple listeners
        listeners.parallelStream().forEach(listener -> listener.receive(packet));
    }

    public void send(IPacket packet) {
        ipLayer.send(packet);
    }

    public void addReceiveListener(IReceiveListener listener) {
        listeners.add(listener);
    }

    public void removeReceiveListener(IReceiveListener listener) {
        listeners.remove(listener);
    }
}
