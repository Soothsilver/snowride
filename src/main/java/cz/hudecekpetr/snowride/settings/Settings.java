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

/**
 * Settings are loaded at the beginning. They're user-global. To access the settings, use {@link Settings#getInstance()}.
 * If you change the settings, use {@link Settings#saveAllSettings()}. Settings are serialized as XML using XStream. I'm not super
 * sure about backwards compatibility so I recommend keeping existing fields in, even if they're no longer used. This
 * is because you don't want to throw away the settings of users as they upgrade to a new version of Snowride.
 */
public class Settings {
    private static Settings instance;

    // Open project:
    public String lastOpenedProject = null;
    public List<String> lastOpenedProjects = new ArrayList<>();

    // Runner:
    public String runScript = "";
    public String runArguments = "";
    public String tbWithoutTags = "";
    public String tbWithTags = "";
    public boolean cbWithoutTags = false;
    public boolean cbWithTags = false;
    public int numberOfSuccessesBeforeEnd = 100;

    // Visual state of the window:
    public double x = -1;
    public double y = -1;
    public double width = 800;
    public double height = 700;
    public boolean maximized = false;

    // Import:
    @SuppressWarnings("unused") // Kept for backwards compatibility with previous versions of the Settings file.
    public String additionalXmlFiles = "";
    public String additionalFolders = "";
    public boolean cbAlsoImportTxtFiles = true;

    // Customization:
    public boolean toolbarReloadAll = true;
    public boolean toolbarDeselectEverything = true;
    public boolean cbShowNonexistentOptionFirst = false;
    public boolean cbAutoExpandSelectedTests = true;
    public boolean cbUseStructureChanged = false;
    public boolean cbRunGarbageCollection = false;
    public boolean cbHighlightSameCells = true;
    public boolean cbUseSystemColorWindow = false;
    public int treeSizeItemHeight = 8;
    public boolean cbAutocompleteVariables = true;
    public ReloadOnChangeStrategy reloadOnChangeStrategy = ReloadOnChangeStrategy.POPUP_DIALOG;

    public static Settings getInstance() {
        if (instance == null) {
            instance = new Settings();
        }
        return instance;
    }

    public static void loadPrimaryFile() {
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
        if (appdata == null) {
            String userHome = System.getProperty("user.home");
            File snowRideHome = new File(userHome, ".snowride");
            snowRideHome.mkdirs();
            return new File(snowRideHome, "settings.xml");
        } else {
            Path path = Paths.get(appdata, "Snowride");
            File fileFolder = path.toFile();
            fileFolder.mkdir();
            return path.resolve("settings.xml").toFile();
        }
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

    /**
     * When I didn't have this, sometimes Snowride serialized -32000 as the "X" value, probably because that's what
     * it was given by JavaFX. That meant that the window was technically open but so far left that the user couldn't
     * see it. To prevent this, we'll disallow X,Y less than 0 during load. The downside is that if your primary monitor
     * is not the leftmost monitor, you will be unable to save the position of Snowride on your left monitor(s).
     */
    private void correctWindowExcesses() {
        if (this.x < 0) {
            this.x = 0;
        }
        if (this.y < 0) {
            this.y = 0;
        }
    }

    public void saveAllSettings() {
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
            if (StringUtils.isBlank(folder)) {
                continue;
            }
            String folderPath = folder.trim();
            File folderAsFile = new File(folderPath);
            files.add(folderAsFile);
        }
        return files;
    }

    public void saveIntoSnow(File saveWhere) {
        SnowFile.saveInto(saveWhere);
    }
}
