package za.co.bc.copy;

import java.io.IOException;
import java.io.OutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;


public class FileHashDb implements HashDb {
    private static final Logger LOGGER = Logger.getLogger(FileHashDb.class.getName());
    
    final private OutputStream out;

    public FileHashDb(OutputStream out) {
        this.out = out;
    }
    
    @Override
    public void add(byte[] hash) throws HashDbException {
        try {
            out.write(hash.length);
            out.write(hash);
        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
            throw new HashDbException(hash);
        }
    }

}
