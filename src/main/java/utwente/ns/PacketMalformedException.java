package utwente.ns;

/**
 * Created by simon on 07.04.17.
 */
public class PacketMalformedException extends Exception {

    public PacketMalformedException() {
        super();
    }

    public PacketMalformedException(String message) {
        super(message);
    }
}
