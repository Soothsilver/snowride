package cz.hudecekpetr.snowride.parser;

import cz.hudecekpetr.snowride.antlr.RobotLexer;
import cz.hudecekpetr.snowride.antlr.RobotParser;
import cz.hudecekpetr.snowride.filesystem.FilesystemWatcher;
import cz.hudecekpetr.snowride.settings.Settings;
import cz.hudecekpetr.snowride.tree.highelements.ExternalResourcesElement;
import cz.hudecekpetr.snowride.tree.highelements.FileSuite;
import cz.hudecekpetr.snowride.tree.highelements.FolderSuite;
import cz.hudecekpetr.snowride.tree.highelements.HighElement;
import cz.hudecekpetr.snowride.tree.RobotFile;
import cz.hudecekpetr.snowride.tree.highelements.Suite;
import cz.hudecekpetr.snowride.ui.LongRunningOperation;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Facade for parsing and loading Robot Framework files and for loading directories, including recursively.
 */
public class GateParser {

    /**
     * Loads a directory and its children, recursively, and parses, but doesn't analyze semantics, and certainly doesn't
     * calculate semantics for cells.
     *
     * @param directoryPath The directory to load. May be relative.
     * @param partOfOperation The progress bar to update as loading progresses.
     * @param partOfProgress When this function completes, this much progress (on a scale of 0 to 1) should be added to the progress bar.
     */
    public FolderSuite loadDirectory(File directoryPath, LongRunningOperation partOfOperation, double partOfProgress) {
        try {
            String name = directoryPath.getName();
            String contents = null;
            File initFile = null;
            List<HighElement> fileSuites = new ArrayList<>();
            File[] files = directoryPath.listFiles();
            if (files == null) {
                throw new RuntimeException("The file '" + directoryPath + "' is not a directory.");
            }
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
        // We support only the text space-separated format.
        // HTML files are deprecated anyway.
        return inFile.getName().toLowerCase().endsWith(".robot") || (inFile.getName().toLowerCase().endsWith(".txt") && Settings.getInstance().cbAlsoImportTxtFiles);
    }

    private boolean isInitFile(File inFile) {
        return inFile.getName().equalsIgnoreCase("__init__.robot") ||
                   (inFile.getName().equalsIgnoreCase("__init__.txt") && Settings.getInstance().cbAlsoImportTxtFiles);
    }

    /**
     * Loads a .txt or .robot Robot Framework file as a file suite. This shouldn't be called for __init__ files.
     * Semantics aren't analyzed.
     * @param inFile Filename to load.
     */
    public FileSuite loadFile(File inFile) throws IOException {
        String name = FilenameUtils.removeExtension(inFile.getName());
        String contents = FileUtils.readFileToString(inFile, "utf-8");
        return new FileSuite(inFile, name, contents);
    }

    /**
     * Parses Robot Framework code and assigns it to the given suite.
     * @param contents Robot Framework text.
     * @param owningSuite The suite that owns the text. This may be a folder suite if the text is from an __init__ file.
     */
    public static RobotFile parse(String contents, Suite owningSuite) {
        // Remove byte order mark. It causes problems to ANTLR:
        if (contents.length() > 0 && contents.charAt(0) == '\uFEFF') {
            contents = contents.substring(1);
        }
        // Parse:
        RobotLexer robotLexer = new RobotLexer(CharStreams.fromString(contents));
        RobotParser robotParser = new RobotParser(new CommonTokenStream(robotLexer));
        AntlrListener listener = new AntlrListener(owningSuite);
        robotParser.addParseListener(listener);
        robotParser.addErrorListener(listener);
        RobotFile file = new RobotFile();
        try {
            file = robotParser.file().File;
        } catch (Exception exception) {
            listener.errors.add(new RuntimeException("Parsing failed. " + exception.getMessage(), exception));
        }
        file.errors = listener.errors;
        // Return:
        return file;
    }

    public ExternalResourcesElement createExternalResourcesElement(List<File> additionalFoldersAsFiles, LongRunningOperation operation, double progressPart) {
        List<HighElement> suites = new ArrayList<>();
        for (File file : additionalFoldersAsFiles) {
            FolderSuite newSuite = loadDirectory(file, operation, progressPart / additionalFoldersAsFiles.size());
            suites.add(newSuite);
        }

        return new ExternalResourcesElement(suites);
    }
}
