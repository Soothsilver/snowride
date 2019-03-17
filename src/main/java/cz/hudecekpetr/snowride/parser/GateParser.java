package cz.hudecekpetr.snowride.parser;

import cz.hudecekpetr.snowride.lexer.LogicalLine;
import cz.hudecekpetr.snowride.tree.FileSuite;
import cz.hudecekpetr.snowride.tree.FolderSuite;
import cz.hudecekpetr.snowride.tree.HighElement;
import cz.hudecekpetr.snowride.tree.RobotFile;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class GateParser {
    private Lexer lexer = new Lexer();

    public FolderSuite loadDirectory(File directoryPath) throws IOException {
        String name = directoryPath.getName();
        String contents = "";
        File initFile = null;
        RobotFile initFileParsed = null;
        List<HighElement> fileSuites = new ArrayList<>();
        for (File inFile : directoryPath.listFiles()) {
            if (inFile.isDirectory()) {
                FolderSuite inThing = loadDirectory(inFile);
                fileSuites.add(inThing);
            } else if (inFile.getName().toLowerCase().equals("__init__.robot")) {
                contents = FileUtils.readFileToString(inFile, "utf-8");
                List<LogicalLine> lexed = lexer.lex(contents);
                Parser parser = new Parser(lexed);
                initFile = inFile;
                initFileParsed = parser.fileSuite();
            } else {
                FileSuite inThing = loadFile(inFile);
                fileSuites.add(inThing);
            }
        }
        return new FolderSuite(initFile, initFileParsed, name, contents, fileSuites);
    }

    private FileSuite loadFile(File inFile) throws IOException {
        String name = inFile.getName();
        String contents = FileUtils.readFileToString(inFile, "utf-8");
        List<LogicalLine> lexed = lexer.lex(contents);
        Parser parser = new Parser(lexed);
        return new FileSuite(inFile, name, contents, parser.fileSuite());

    }
}
