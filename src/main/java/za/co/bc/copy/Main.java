package za.co.bc.copy;

import za.co.bc.copy.command.CopyCommand;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import za.co.bc.copy.command.BuildCommand;
import za.co.bc.copy.command.Command;
import za.co.bc.copy.service.FileService;

public class Main {
    private static final Logger LOGGER = Logger.getLogger(Main.class.getName());
    
    private HashLookup hashLookup(Path dbPath) throws IOException {
        if (!Files.exists(dbPath)) {
            return new SetHashLookup(new HashSet<>());
        }
        LOGGER.log(Level.INFO, "Loading {0}", dbPath);
        try (InputStream in = Files.newInputStream(dbPath)) {
            final Set<BigInteger> hashes = new HashSet<>();
            final byte[] buffer = new byte[64];
            int len = in.read();
            while (len != -1) {
                int read = in.read(buffer, 0, len);
                if (read < len) {
                    throw new RuntimeException(String.format("Read %d expected length %d ; should make this more robust", read, len));
                }
                final BigInteger hash = new BigInteger(Arrays.copyOf(buffer, len));
                hashes.add(hash);
                len = in.read();
            }
            LOGGER.log(Level.INFO, "Hash set size {0}", hashes.size());
            return new SetHashLookup(hashes);
        }
    }
    
    private FileService fileService() {
        return new FileService();
    }
    
    private Hasher getHasher() throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("md5");
        final Hasher hasher = new SimpleHasher(md, fileService());
        return hasher;
    }

    private HashDb hashDb(OutputStream out) {
        return new FileHashDb(out);
    }
    
    public Command buildCommand(String db, String target, Hasher hasher) {
        final Path dbPath = Paths.get(db);
        final Path targetPath = Paths.get(target);
        return new BuildCommand(hasher, dbPath, targetPath);
    };
    
    public Command copyCommand(HashDb hashDb, HashLookup hashLookup, Hasher hasher, Path source, Path dest) {
        return new CopyCommand(hashDb, hashLookup, hasher, source, dest);
    }
    
    public static void main(String[] args) throws IOException, NoSuchAlgorithmException {
        LOGGER.log(Level.INFO, "Command line arguments: {0}", Arrays.toString(args));

        if (args.length == 0) {
            LOGGER.info("No command given");
            return;
        }
        if (args.length == 1) {
            LOGGER.info("No db given");
            return;
        }
        
        Main main = new Main();
        final Hasher hasher = main.getHasher();
        if (hasher == null) {
            LOGGER.severe("Could create hasher");
            return;
        }
        final String db = args[1];
        final Path dbPath = Paths.get(db);
        
        switch (args[0].toUpperCase()) {
            case "BUILD":
                final String target = args[2];
                final Command build = main.buildCommand(db, target, hasher);
                build.execute();
                break;
            case "COPY":
                final String source = args[2];
                final String dest = args[3];
                final Path sourcePath = Paths.get(source);
                final Path destPath = Paths.get(dest);
                
                final HashLookup hashLookup = main.hashLookup(dbPath);
                try (OutputStream out = Files.newOutputStream(dbPath, StandardOpenOption.APPEND, StandardOpenOption.CREATE)) {
                    final HashDb hashDb = main.hashDb(out);
                    final Command copyCommand = main.copyCommand(hashDb, hashLookup, hasher, sourcePath, destPath);
                    copyCommand.execute();
                }
                break;
            default:
                throw new AssertionError();
        }
    }

    
}
