package cz.hudecekpetr.snowride.settings;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.converters.reflection.PureJavaReflectionProvider;
import com.thoughtworks.xstream.io.xml.StaxDriver;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;

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


    @SuppressWarnings("unused") // Kept for backwards compatibility with previous versions of the Settings file.
    public String additionalXmlFiles = "";
    public String additionalFolders = "";
    public boolean cbAlsoImportTxtFiles = true;
    public boolean toolbarReloadAll = true;
    public boolean toolbarDeselectEverything = true;
    public boolean cbShowNonexistentOptionFirst = false;

    public static Settings getInstance() {
        if (instance == null) {
            instance = new Settings();
        }
        return instance;
    }

    public static void load() {
        try {
            XStream xStream = new XStream(new PureJavaReflectionProvider(), new StaxDriver());
            XStream.setupDefaultSecurity(xStream);
            xStream.allowTypesByWildcard(new String[]{"cz.**"});
            xStream.ignoreUnknownElements();
            instance = (Settings) xStream.fromXML(getFile());
            instance.correctWindowExcesses();
        } catch (Exception exception) {
            System.err.println("Could not load settings file " + getFile().toString() + ". If this is the first time " +
                    "you launch Snowride, this is fine and ignore this message.");
            instance = new Settings();
        }
    }

    private static File getFile() {
        String appdata = System.getenv("APPDATA");
        Path path = Paths.get(appdata, "Snowride");
        File fileFolder = path.toFile();
        fileFolder.mkdir();
        return path.resolve("settings.xml").toFile();
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

    private void correctWindowExcesses() {
        if (this.x < 0) {
            this.x = 0;
        }
        if (this.y < 0) {
            this.y = 0;
        }
    }

    public void save() {
        XStream xStream = new XStream(new PureJavaReflectionProvider(), new StaxDriver());
        String data = xStream.toXML(this);
        File settingsFile = getFile();
        try {
            FileUtils.write(settingsFile, data, "utf-8");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public List<File> getAdditionalFoldersAsFiles() {
        List<File> files = new ArrayList<>();
        String[] foldersSplit = StringUtils.split(additionalFolders, '\n');
        for (String folder : foldersSplit) {
            String folderPath = folder.trim();
            File folderAsFile = new File(folderPath);
            files.add(folderAsFile);
        }
        return files;
    }
}
