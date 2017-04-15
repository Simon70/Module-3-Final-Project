package utwente.ns.ip;

import utwente.ns.IReceiveListener;

import java.io.Closeable;
import java.io.IOException;

/**
 * Created by Niels Overkamp on 13-Apr-17.
 */
public interface IHRP4Socket extends IReceiveListener, Closeable {

    public void send(byte[] data, int dstAddress, short dstPort) throws IOException;
    public void addReceiveListener(IReceiveListener listener);
    public void removeReceiveListener(IReceiveListener listener);
    public short getDstPort();
    
}
