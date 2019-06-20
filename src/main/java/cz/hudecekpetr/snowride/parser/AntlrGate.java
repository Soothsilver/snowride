package cz.hudecekpetr.snowride.parser;

import cz.hudecekpetr.snowride.antlr.RobotLexer;
import cz.hudecekpetr.snowride.antlr.RobotParser;
import cz.hudecekpetr.snowride.tree.RobotFile;
import cz.hudecekpetr.snowride.tree.highelements.Suite;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;

public class AntlrGate {
    public RobotFile parse(String fileContents, Suite owningSuite) {
        // Remove byte order mark:
        if (fileContents.length() > 0 && fileContents.charAt(0) == '\uFEFF') {
            fileContents = fileContents.substring(1);
        }
        RobotLexer robotLexer = new RobotLexer(CharStreams.fromString(fileContents));
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
        return file;
    }
}
