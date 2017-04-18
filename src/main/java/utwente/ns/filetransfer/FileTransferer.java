package utwente.ns.filetransfer;

import utwente.ns.tcp.RTP4Connection;
import utwente.ns.tcp.RTP4Socket;
import utwente.ns.ui.UniversalCommunicator;

import javax.swing.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * Created by simon on 17.04.17.
 */
public class FileTransferer {
    private static final int PORT = 21;
    private static final int PART_SIZE = 10 * 1024;
    private final UniversalCommunicator gui;
    private File file;
    private String address;

    /**
     * Send a file.
     *
     * @param gui     to track progress
     * @param file    to be sent
     * @param address to be sent to
     */
    public FileTransferer(UniversalCommunicator gui, File file, String address) {
        this.gui = gui;
        this.file = file;
        this.address = address;
        Thread transferThread = new Thread(this::sendTheFile);
        transferThread.setDaemon(true);
        transferThread.setName("FileTransfer");
        transferThread.start();
    }

    public FileTransferer(UniversalCommunicator gui) {
        this.gui = gui;
        Thread transferThread = new Thread(this::receiveTheFile);
        transferThread.setDaemon(true);
        transferThread.setName("FileTransfer");
        transferThread.start();
    }

    private void sendTheFile() {
        gui.addFileTransferLogMessage("Starting Transfer...");
        try {
            gui.addFileTransferLogMessage("Connecting...");
            RTP4Connection connection = gui.getNetworkStack().getRtp4Layer().connect(address, PORT);
            gui.addFileTransferLogMessage("Sending FileInfo...");
            connection.send(new FileInfoPacket(file.getName(), (int) file.length()).toBytes());
            FileInputStream fis = new FileInputStream(file);
            byte[] sendBuffer = new byte[PART_SIZE];
            for (int i = 0; i < file.length() / PART_SIZE + 1; i++) {
                gui.addFileTransferLogMessage("Sending Packet! (" + i + 1 + "/" + file.length() / PART_SIZE + 1 + ")");
                gui.setProgress(i, ((int) file.length()) / PART_SIZE + 1);
                int read = fis.read(sendBuffer);
                if (read < PART_SIZE) {
                    byte[] smallerSendBuffer = new byte[read];
                    System.arraycopy(sendBuffer, 0, smallerSendBuffer, 0, read);
                    connection.send(smallerSendBuffer);
                    connection.close();
                    gui.addFileTransferLogMessage("File sent!");
                    gui.setProgress(100,100);
                } else
                    connection.send(sendBuffer);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void receiveTheFile() {
        gui.addFileTransferLogMessage("Waiting for FileInfo...");
        try {
            RTP4Socket socket = gui.getNetworkStack().getRtp4Layer().open(PORT);
            RTP4Connection connection = socket.accept();
            FileInfoPacket fileInfo = new FileInfoPacket(connection.receive());
            gui.addFileTransferLogMessage("Received FileInfo!");
            JFileChooser fc = new JFileChooser();
            fc.setName(fileInfo.name);
            fc.showSaveDialog(gui.getMainPanel());
            File selectedFile = fc.getSelectedFile();
            FileOutputStream fos = new FileOutputStream(selectedFile);
            for (int i = 0; i < fileInfo.parts; i++) {
                byte[] part = connection.receive();
                fos.write(part, i * PART_SIZE, part.length);
                gui.addFileTransferLogMessage("Received Packet! (" + i + 1 + "/" + fileInfo.parts + ")");
                gui.setProgress(i, fileInfo.parts);
            }
            gui.addFileTransferLogMessage("File received!");
            gui.setProgress(100,100);
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    private class FileInfoPacket {
        private final String name;
        private final int size;
        private final int parts;

        public FileInfoPacket(String name, int size) {
            this.name = name;
            this.size = size;
            parts = size / PART_SIZE + 1;
        }

        private FileInfoPacket(byte[] bytes) {
            ByteBuffer buf = ByteBuffer.wrap(bytes);
            size = buf.getInt();
            byte[] nameData = new byte[bytes.length - 4];
            System.arraycopy(bytes, 4, nameData, 0, nameData.length);
            name = new String(nameData);
            parts = size / PART_SIZE + 1;
        }

        private byte[] toBytes() {
            byte[] data = name.getBytes();
            ByteBuffer buf = ByteBuffer.allocate(data.length + 4);
            buf.putInt(size);
            buf.put(data);
            return buf.array();
        }
    }
}
