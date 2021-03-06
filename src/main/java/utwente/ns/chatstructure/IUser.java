package utwente.ns.chatstructure;

/**
 * Created by simon on 14.04.17.
 */
public interface IUser {
    String getName();

    String getFingerprint();

    boolean isConfirmed();

    String getUniqueID();

    String getAddress();
}
