package cz.hudecekpetr.snowride.settings;

import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Properties;

public class SnowFile {
    public static void loadSnowFile(java.io.File snowFile) {

        try {
            Properties properties = new Properties();
            properties.load(new FileInputStream(snowFile));
            String directory = properties.getProperty("Directory");
            String additionalPaths = properties.getProperty("AdditionalPaths");
            String runnerScript = properties.getProperty("RunnerScript");
            if (directory != null) {
                Settings.getInstance().lastOpenedProject = directory.trim();
            }
            if (additionalPaths != null) {
                Settings.getInstance().additionalFolders = StringUtils.join(Arrays.stream(StringUtils.splitByWholeSeparator(additionalPaths, ";")).map(String::trim).iterator(), "\n");
            }
            if (runnerScript != null) {
                Settings.getInstance().runScript = runnerScript.trim();
            }
            Settings.getInstance().saveAllSettings();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    public static void saveInto(File saveWhere) {
        try {
            Properties properties = new Properties();
            properties.setProperty("Directory", Settings.getInstance().lastOpenedProject);
            properties.setProperty("AdditionalPaths",
                    StringUtils.join(
                            Arrays.stream(
                                    StringUtils.splitByWholeSeparator(Settings.getInstance().additionalFolders, "\n")
                            ).map(String::trim).iterator()
                            ,
                            ";")
            );
            properties.setProperty("RunnerScript", Settings.getInstance().runScript);
            properties.store(new FileOutputStream(saveWhere), "This is a Snowride project file.");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
