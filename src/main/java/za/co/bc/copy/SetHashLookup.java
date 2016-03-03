package za.co.bc.copy;

import java.math.BigInteger;
import java.util.Set;


public class SetHashLookup implements HashLookup {
    private final Set<BigInteger> hashes;

    public SetHashLookup(Set<BigInteger> hashes) {
        this.hashes = hashes;
    }
    
    
    @Override
    public boolean contains(byte[] hash) {
        final BigInteger hashval = new BigInteger(hash);
        return hashes.contains(hashval);
    }
    
}
