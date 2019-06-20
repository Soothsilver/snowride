package cz.hudecekpetr.snowride.semantics.externallibraries;

import cz.hudecekpetr.snowride.Extensions;
import cz.hudecekpetr.snowride.settings.Settings;
import javafx.application.Platform;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ReloadExternalLibraries {
    private static ExecutorService executor = Executors.newFixedThreadPool(10);
    private static Path pythonXmlFilesFolder;

    private static Path getPythonXmlFilesFolder() {
        if (pythonXmlFilesFolder == null) {
            try {
                pythonXmlFilesFolder = Files.createTempDirectory("SnowrideExternal");
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return pythonXmlFilesFolder;
    }

    public static void reload(Runnable callbackOnUIThread) {
        List<File> folders = Settings.getInstance().getAdditionalFoldersAsFiles();
        executor.submit(() -> {
            try {
                for (File folderAsFile : folders) {
                    // XML libraries
                    File[] xmlFiles = folderAsFile.listFiles((file) -> file.getName().endsWith(".xml"));
                    for (File xmlFile : xmlFiles) {
                        try (
                            InputStream xmlStream = new FileInputStream(xmlFile)) {
                            ExternalLibrary library = ExternalLibrary.loadFromInputStream(xmlStream, LibraryKind.XML);
                            ExternalLibrary.knownExternalLibraries.put(library.getName(), library);
                        } catch (Exception anyException) {
                            System.out.println("The file '" + xmlFile.getName() + "' could not be parsed. Maybe it's not a libdoc XML file.");
                        }
                    }

                    // Python libraries
                    File[] pythonFiles = folderAsFile.listFiles((file) -> file.getName().endsWith(".py"));
                    for (File pythonFile : pythonFiles) {
                        try {
                            File targetFile = File.createTempFile("pythonlibrary", ".xml", getPythonXmlFilesFolder().toFile());
                            Process libdoc = Runtime.getRuntime().exec(new String[] { "python", "-m", "robot.libdoc", pythonFile.getAbsolutePath(), targetFile.getAbsolutePath() });
                            if (libdoc.waitFor() == 0) {
                                InputStream xmlStream = new FileInputStream(targetFile);
                                ExternalLibrary library = ExternalLibrary.loadFromInputStream(xmlStream, LibraryKind.PYTHON);
                                ExternalLibrary.knownExternalLibraries.put(library.getName(), library);
                            } else {
                                System.out.println("The file '" + pythonFile.getName() + "' could not be libdoc'd because libdoc returned a nonzero exit status. Maybe it's not a Robot Framework Python library file or you don't have libdoc.");
                            }
                        } catch (Exception anyException) {
                            System.out.println("The file '" + pythonFile.getName() + "' could not be libdoc'd because of an exception. Maybe it's not a Robot Framework Python library file or you don't have libdoc." + Extensions.toStringWithTrace(anyException));
                        }
                    }
                }
                Platform.runLater(callbackOnUIThread);
            } catch (Exception ex) {
                Platform.runLater(() -> {
                    throw new RuntimeException(ex);
                });
            }
        });
    }
}
