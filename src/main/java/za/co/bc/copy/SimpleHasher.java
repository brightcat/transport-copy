package za.co.bc.copy;

import java.io.IOException;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import za.co.bc.copy.service.FileService;

public class SimpleHasher implements Hasher {

    private static final Logger LOGGER = Logger.getLogger(SimpleHasher.class.getName());

    private final MessageDigest md;
    private final FileService fileService;

    public SimpleHasher(MessageDigest md, FileService fileService) {
        this.md = md;
        this.fileService = fileService;
    }

    @Override
    public byte[] hash(Path path) {
        try {
            fileService.read(path, (byte[] buffer) -> {
                md.update(buffer);
            });
            final byte[] digest = md.digest();
            md.reset();
            return digest;
        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
            return null;
        }
    }
}
