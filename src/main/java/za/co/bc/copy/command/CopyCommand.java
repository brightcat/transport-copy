package za.co.bc.copy.command;

import java.io.IOException;
import java.nio.file.CopyOption;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.logging.Level;
import java.util.logging.Logger;
import za.co.bc.copy.HashDb;
import za.co.bc.copy.HashDbException;
import za.co.bc.copy.HashLookup;
import za.co.bc.copy.Hasher;

public class CopyCommand implements Command {

    private static final Logger LOGGER = Logger.getLogger(CopyCommand.class.getName());

    private final HashDb hashDb;
    private final HashLookup hashLookup;
    private final Hasher hasher;
    private final Path source;
    private final Path dest;

    public CopyCommand(HashDb hashDb, HashLookup hashLookup, Hasher hasher, Path source, Path dest) {
        this.hashDb = hashDb;
        this.hashLookup = hashLookup;
        this.hasher = hasher;
        this.source = source;
        this.dest = dest;
    }

    @Override
    public void execute() {
        final SimpleFileVisitor<Path> simpleFileVisitor = noFilter();

        try {
            Files.walkFileTree(source, simpleFileVisitor);
        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
        }
    }

    private SimpleFileVisitor<Path> noFilter() {
        return new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                final byte[] hash = hasher.hash(file);
                if (hash == null) {
                    throw new IOException(String.format("Could not hash [%s]", file));
                }
                if (!hashLookup.contains(hash)) {

                    final Path relativize = source.relativize(file);
                    final Path target = dest.resolve(relativize);

                    LOGGER.log(Level.INFO, "{0} to {1}", new Object[]{file, target});

                    final Path parent = target.getParent();
                    if (!Files.exists(parent)) {
                        Files.createDirectories(parent);
                    }
                    
                    Files.copy(file, target, StandardCopyOption.COPY_ATTRIBUTES);
                    
                    // copy
                    try {
                        hashDb.add(hash);
                    } catch (HashDbException ex) {
                        LOGGER.log(Level.SEVERE, null, ex);
                        throw new IOException(ex);
                    }
                }

                return super.visitFile(file, attrs); //To change body of generated methods, choose Tools | Templates.
            }

        };
    }

}
