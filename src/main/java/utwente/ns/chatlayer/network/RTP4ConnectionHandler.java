package utwente.ns.chatlayer.network;

import lombok.AllArgsConstructor;
import utwente.ns.Util;
import utwente.ns.tcp.RTP4Connection;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.UnknownHostException;

/**
 * Created by Harindu Perera on 4/17/17.
 */
@AllArgsConstructor
public class RTP4ConnectionHandler implements Runnable {

    private final RTP4Connection connection;
    private final IRequestHandler handler;

    public static void handleConnection(RTP4Connection connection, IRequestHandler handler) {
        RTP4ConnectionHandler connectionHandler = new RTP4ConnectionHandler(connection, handler);
        new Thread(connectionHandler).start();
    }

    @Override
    public void run() {
        // TODO: address the whole closing issue
        try (RTP4Connection conn = connection) {
            ByteArrayOutputStream requestBuffer = new ByteArrayOutputStream();
            try {
                while (!conn.isClosed()) {
                    requestBuffer.write(conn.receive());
                }
                requestBuffer.close();
            } catch (IOException | InterruptedException e) {
                return;
            }
            if (requestBuffer.size() == 0) return;
            try {
                conn.send(handler.handleData(Util.intToAddressString(conn.getAddress()), conn.getPort(), requestBuffer.toByteArray()));
            } catch (UnknownHostException e) {
                e.printStackTrace();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
