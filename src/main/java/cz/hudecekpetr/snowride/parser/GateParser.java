package cz.hudecekpetr.snowride.parser;

import cz.hudecekpetr.snowride.filesystem.FilesystemWatcher;
import cz.hudecekpetr.snowride.lexer.LogicalLine;
import cz.hudecekpetr.snowride.listener.AntlrGate;
import cz.hudecekpetr.snowride.settings.Settings;
import cz.hudecekpetr.snowride.tree.ExternalResourcesElement;
import cz.hudecekpetr.snowride.tree.FileSuite;
import cz.hudecekpetr.snowride.tree.FolderSuite;
import cz.hudecekpetr.snowride.tree.HighElement;
import cz.hudecekpetr.snowride.tree.RobotFile;
import cz.hudecekpetr.snowride.tree.Suite;
import cz.hudecekpetr.snowride.ui.LongRunningOperation;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class GateParser {
    private static AntlrGate gate = new AntlrGate();

    public FolderSuite loadDirectory(File directoryPath, LongRunningOperation partOfOperation, double partOfProgress) {
        try {
            String name = directoryPath.getName();
            String contents = null;
            File initFile = null;
            List<HighElement> fileSuites = new ArrayList<>();
            File[] files = directoryPath.listFiles();
            double perFile = partOfProgress / files.length;
            for (File inFile : files) {
                if (inFile.isDirectory()) {
                    FolderSuite inThing = loadDirectory(inFile, partOfOperation, perFile);
                    fileSuites.add(inThing);
                } else if (isInitFile(inFile)) {
                    contents = FileUtils.readFileToString(inFile, "utf-8");
                    initFile = inFile;
                    partOfOperation.success(perFile);
                } else if (endsWithRobotExtension(inFile)) {
                    FileSuite inThing = loadFile(inFile);
                    fileSuites.add(inThing);
                    partOfOperation.success(perFile);
                } else {
                    // We can ignore this file.
                    partOfOperation.success(perFile);
                }
            }
            FolderSuite folderSuite = new FolderSuite(directoryPath, initFile, name, contents, fileSuites);
            folderSuite.reparse();
            for(HighElement fs : fileSuites) {
                fs.parent = folderSuite;
            }
            FilesystemWatcher.getInstance().startWatching(directoryPath);
            return folderSuite;
        } catch (IOException exc) {
            throw new RuntimeException(exc);
        }
    }

    private boolean endsWithRobotExtension(File inFile) {
        return inFile.getName().toLowerCase().endsWith(".robot") || (inFile.getName().toLowerCase().endsWith(".txt") && Settings.getInstance().cbAlsoImportTxtFiles);
    }

    private boolean isInitFile(File inFile) {
        return inFile.getName().equalsIgnoreCase("__init__.robot") ||
                   (inFile.getName().equalsIgnoreCase("__init__.txt") && Settings.getInstance().cbAlsoImportTxtFiles);
    }

    public FileSuite loadFile(File inFile) throws IOException {
        String name = FilenameUtils.removeExtension(inFile.getName());
        String contents = FileUtils.readFileToString(inFile, "utf-8");
        return new FileSuite(inFile, name, contents);
    }

    public static RobotFile parse(String contents, Suite owningSuite) {
        return gate.parse(contents, owningSuite);
    }

    public ExternalResourcesElement createExternalResourcesElement(List<File> additionalFoldersAsFiles, LongRunningOperation operation, double progressPart) {
        List<HighElement> suites = new ArrayList<>();
        for (File file : additionalFoldersAsFiles) {
            FolderSuite newSuite = loadDirectory(file, operation, progressPart / additionalFoldersAsFiles.size());
            suites.add(newSuite);
        }

        ExternalResourcesElement externalResourcesElement = new ExternalResourcesElement(suites);
        return externalResourcesElement;
    }
}
