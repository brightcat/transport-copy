package za.co.bc.copy;

import java.util.Arrays;

public class HashDbException extends Exception {
    private final byte[] hash;
    
    public HashDbException(byte[] hash) {
        this.hash = Arrays.copyOf(hash, hash.length);
    }

    public byte[] getHash() {
        return Arrays.copyOf(hash, hash.length);
    }
}
