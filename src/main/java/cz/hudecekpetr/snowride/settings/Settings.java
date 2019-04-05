package cz.hudecekpetr.snowride.settings;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.StaxDriver;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class Settings {
    private static Settings instance;

    public String lastOpenedProject = null;
    public List<String> lastOpenedProjects = new ArrayList<String>();
    public String runScript = "";
    public String runArguments = "";
    public String tbWithoutTags = "";
    public String tbWithTags = "";
    public boolean cbWithoutTags = false;
    public boolean cbWithTags = false;
    public double x = -1;
    public double y = -1;
    public double width = 800;
    public double height = 700;
    public boolean maximized = false;
    public String additionalXmlFiles = "";

    public static Settings getInstance() {
        if (instance == null) {
            instance = new Settings();
        }
        return instance;
    }

    public void addToRecentlyOpen(String path) {
        if (lastOpenedProjects.size() > 0 && lastOpenedProjects.get(lastOpenedProjects.size() - 1).equals(path)) {
            // reloading the last one doesn't do anything
            return;
        }
        lastOpenedProjects.add(path);
        if (lastOpenedProjects.size() > 4) {
            lastOpenedProjects.remove(0);
        }
    }

    public static void load() {
        try {
            XStream xStream = new XStream(new StaxDriver());
            XStream.setupDefaultSecurity(xStream);
            xStream.allowTypesByWildcard(new String[] { "cz.**" });
            Settings settings = (Settings) xStream.fromXML(getFile());
            instance = settings;
        } catch (Exception exception) {
            System.err.println("Could not load settings file " + getFile().toString() + ". If this is the first time " +
                    "you launch Snowride, this is fine and ignore this message. Actual exception: " + exception.getMessage());
            instance = new Settings();
        }
    }

    public void save() {
        XStream xStream = new XStream(new StaxDriver());
        String data = xStream.toXML(this);
        File settingsFile = getFile();
        try {
            FileUtils.write(settingsFile, data, "utf-8");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static File getFile() {
        String appdata = System.getenv("APPDATA");
        Path path = Paths.get(appdata, "Snowride");
        File fileFolder = path.toFile();
        fileFolder.mkdir();
        File fileFile = path.resolve("settings.xml").toFile();
        return fileFile;
    }
}
