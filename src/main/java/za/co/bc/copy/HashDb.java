package za.co.bc.copy;

public interface HashDb {
    void add(byte[] hash) throws HashDbException;
}
