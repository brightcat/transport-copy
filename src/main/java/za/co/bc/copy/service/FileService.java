package za.co.bc.copy.service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

public class FileService {

    private static final int BUFFER_SIZE = 64 * 1024 * 1024;

    public void read(Path path, ByteVisitor byteVisitor) throws IOException {
        final byte[] buffer = new byte[BUFFER_SIZE];
        try (InputStream is = Files.newInputStream(path)) {
            int read = is.read(buffer);
            while (read != -1) {
                if (read < buffer.length) {
                    byteVisitor.visit(Arrays.copyOf(buffer, read));
                } else {
                    byteVisitor.visit(buffer);
                }
                read = is.read(buffer);
            }
        }
    }
}
