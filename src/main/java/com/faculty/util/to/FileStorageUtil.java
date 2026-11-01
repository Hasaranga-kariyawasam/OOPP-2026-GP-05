package com.faculty.util.to;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

/**
 * Copies a file chosen by the user into the application's uploads/ folder
 * (uploads/medicals/..., uploads/profiles/...) and returns the relative path
 * that is stored in the database (medical_record.document_path, users.profile_picture_path).
 */
public final class FileStorageUtil {

    private static final String ROOT = "uploads";

    private FileStorageUtil() { }

    /**
     * @param source   file picked in the FileChooser
     * @param subFolder e.g. "medicals" or "profiles"
     * @param baseName  file name WITHOUT extension, e.g. "TG_2024_1013_2026-08-05"
     * @return path like "uploads/medicals/TG_2024_1013_2026-08-05.pdf" (always with '/')
     */
    public static String store(File source, String subFolder, String baseName) throws IOException {
        String extension = extensionOf(source.getName());
        String safeName = baseName.replaceAll("[^A-Za-z0-9_.-]", "_") + extension;

        Path folder = Path.of(ROOT, subFolder);
        Files.createDirectories(folder);
        Files.copy(source.toPath(), folder.resolve(safeName), StandardCopyOption.REPLACE_EXISTING);

        return ROOT + "/" + subFolder + "/" + safeName;
    }

    /** ".pdf" / ".png" ... (lower case) or "" when the file has no extension. */
    private static String extensionOf(String fileName) {
        int dot = fileName.lastIndexOf('.');
        return dot < 0 ? "" : fileName.substring(dot).toLowerCase();
    }
}
