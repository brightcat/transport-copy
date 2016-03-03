package za.co.bc.copy;

import java.nio.file.Path;

public interface Hasher {
    byte[] hash(Path path);
}
