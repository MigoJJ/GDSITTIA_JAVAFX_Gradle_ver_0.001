package com.ittia.gds;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Manages and provides access to essential application directories.
 * It initializes paths based on the environment ('dev' or 'prod')
 * and ensures they exist on startup using standard console output.
 */
public final class EntryDir {

    // --- Environment & Base Path Configuration ---
    private static final String ENV = System.getProperty("app.env", "dev");
    private static final Path CURRENT_DIR = Paths.get(System.getProperty("user.dir"));
    private static final Path basePath = "prod".equals(ENV) ? CURRENT_DIR : CURRENT_DIR.resolve("src");

    // --- Application Directory Paths (Created on Class Load) ---
    public static final Path HOME_DIR = create(basePath.resolve("je/pense/doro"));
    public static final Path BACKUP_DIR = create(HOME_DIR.resolve("tripikata/rescue"));
    public static final Path SUPPORT_DIR = create(HOME_DIR.resolve("support/EMR_support_Folder"));
    public static final Path dbDir = create(HOME_DIR.resolve("chartplate/filecontrol/database"));

    /**
     * Private helper to create a directory and return its path.
     * If creation fails, it throws an unchecked exception to halt startup.
     */
    private static Path create(Path directory) {
        try {
            if (Files.notExists(directory)) {
                Files.createDirectories(directory);
                System.out.println("Created directory: " + directory);
            }
            return directory;
        } catch (IOException e) {
            System.err.println("FATAL: Could not create required directory: " + directory);
            e.printStackTrace(); // Print stack trace for debugging
            throw new IllegalStateException("Failed to create required directory: " + directory, e);
        }
    }

    /**
     * Gets the full path for a file within the "Thyroid" support sub-directory.
     * @param fileName The name of the file.
     * @return The full, platform-independent Path to the file.
     */
    public static Path getThyroidFilePath(String fileName) {
        return SUPPORT_DIR.resolve("Thyroid").resolve(fileName);
    }
    
    // Private constructor to prevent instantiation of this utility class
    private EntryDir() {}
}