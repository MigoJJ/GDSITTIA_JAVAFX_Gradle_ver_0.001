package com.ittia.gds.ui.mainframe.file;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

public class File_copy {
    public static void main(String filePath, String newFilePath) {

        Path source = Paths.get(filePath);
        Path destination = Paths.get(newFilePath);

        try {
            Files.copy(source, destination, StandardCopyOption.REPLACE_EXISTING);
//            System.out.println("File copied successfully.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
