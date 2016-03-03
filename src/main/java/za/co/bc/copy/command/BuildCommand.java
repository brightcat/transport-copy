package za.co.bc.copy.command;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import za.co.bc.copy.HashDbException;
import za.co.bc.copy.FileHashDb;
import za.co.bc.copy.HashDb;
import za.co.bc.copy.Hasher;


public class BuildCommand implements Command {
    private static final Logger LOGGER = Logger.getLogger(BuildCommand.class.getName());
    
    final private Hasher hasher;
    final private Path db;
    final private Path target;

    public BuildCommand(Hasher hasher, Path db, Path target) {
        this.hasher = hasher;
        this.db = db;
        this.target = target;
    }
    
    private void build(HashDb hashDb, Path path, Hasher hasher) throws IOException {
        final SimpleFileVisitor<Path> simpleFileVisitor = new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                try {
                    final byte[] hash = hasher.hash(file);
                    if (hash == null) {
                        throw new IOException("Could not read hash on " + file);
                    }
                    LOGGER.log(Level.INFO, () -> String.format("%s hashed to %s", file.toString(), Arrays.toString(hash)));
                    hashDb.add(hash);
                    return super.visitFile(file, attrs); //To change body of generated methods, choose Tools | Templates.
                } catch (HashDbException ex) {
                    LOGGER.log(Level.SEVERE, null, ex);
                    return FileVisitResult.TERMINATE;
                }
            }
            
        };
        Files.walkFileTree(path, simpleFileVisitor);
    }
    
    private void build(Path dbPath, Path targetPath) {
        try (OutputStream out = Files.newOutputStream(dbPath)) {
            final HashDb hashDb = new FileHashDb(out);
            build(hashDb, targetPath, hasher);
        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void execute() {
        build(db, target);
    }
}
