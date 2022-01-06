package cz.hudecekpetr.snowride.settings;

import cz.hudecekpetr.snowride.ui.MainForm;

import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Properties;
import java.util.stream.Collectors;

public class SnowFile {
    public static void loadSnowFile(File snowFile) {

        try {
            Properties properties = new Properties();
            properties.load(new FileInputStream(snowFile));
            String directory = toAbsolutePaths(snowFile, properties.getProperty("Directory"));
            String additionalPaths = toAbsolutePaths(snowFile, properties.getProperty("AdditionalPaths"));
            String runnerScript = toAbsolutePaths(snowFile, properties.getProperty("RunnerScript"));
            String runArguments = properties.getProperty("RunnerArguments");
            String cbWithoutTags = properties.getProperty("CheckboxRunWithoutTags");
            String cbWithTags = properties.getProperty("CheckboxRunWithTags");
            String tbWithoutTags = properties.getProperty("TextboxRunWithoutTags");
            String tbWithTags = properties.getProperty("TextboxRunWithTags");

            MainForm.INSTANCE.runTab.suppressRunNumberChangeNotifications = true;
            if (directory != null) {
                Settings.getInstance().lastOpenedProject = directory.trim();
            }
            if (additionalPaths != null) {
                Settings.getInstance().additionalFolders = StringUtils.join(Arrays.stream(StringUtils.splitByWholeSeparator(additionalPaths, ";")).map(String::trim).iterator(), "\n");
            }
            if (runnerScript != null) {
                Settings.getInstance().runScript = runnerScript.trim();
                MainForm.INSTANCE.runTab.tbScript.setText(Settings.getInstance().runScript);
            }
            if (cbWithoutTags != null) {
                Settings.getInstance().cbWithoutTags = cbWithoutTags.trim().equalsIgnoreCase("True");

                    MainForm.INSTANCE.runTab.cbWithoutTags.setSelected(Settings.getInstance().cbWithoutTags);

            }
            if (cbWithTags != null) {
                Settings.getInstance().cbWithTags = cbWithTags.trim().equalsIgnoreCase("True");
                    MainForm.INSTANCE.runTab.cbWithTags.setSelected(Settings.getInstance().cbWithTags);

            }
            if (tbWithoutTags != null) {
                Settings.getInstance().tbWithoutTags = tbWithoutTags.trim();

                MainForm.INSTANCE.runTab.tbWithoutTags.setText(Settings.getInstance().tbWithoutTags);

            }
            if (tbWithTags != null) {
                Settings.getInstance().tbWithTags = tbWithTags.trim();

                MainForm.INSTANCE.runTab.tbWithTags.setText(Settings.getInstance().tbWithTags);

            }
            if (runArguments != null) {
                Settings.getInstance().runArguments = runArguments.trim();

                    MainForm.INSTANCE.runTab.tbArguments.setText(Settings.getInstance().runArguments);

            }
            MainForm.INSTANCE.runTab.suppressRunNumberChangeNotifications = false;
            Settings.getInstance().saveAllSettings();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    private static String toAbsolutePaths(File snowFile, String inputPaths) throws IOException {
        if (StringUtils.isBlank(inputPaths)) {
            return inputPaths;
        }
        return Arrays.stream(inputPaths.split(";")).map(path -> {
            File file = new File(path);
            if (!file.isAbsolute()) {
                file = new File(snowFile.getParentFile(), path);
            }
            try {
                return file.getCanonicalPath();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }).collect(Collectors.joining(";"));
    }

    public static void saveInto(File saveWhere) {
        try {
            Properties properties = new Properties();
            properties.setProperty("Directory", MainForm.INSTANCE.getRootDirectoryElement().directoryPath.getAbsolutePath());
            properties.setProperty("AdditionalPaths",
                    StringUtils.join(
                            Arrays.stream(
                                    StringUtils.splitByWholeSeparator(Settings.getInstance().additionalFolders, "\n")
                            ).map(String::trim).iterator()
                            ,
                            ";")
            );
            properties.setProperty("RunnerScript", Settings.getInstance().runScript);
            properties.setProperty("RunnerArguments", Settings.getInstance().runArguments);
            properties.setProperty("CheckboxRunWithoutTags", Settings.getInstance().cbWithoutTags ? "True" : "False");
            properties.setProperty("CheckboxRunWithTags", Settings.getInstance().cbWithTags ? "True" : "False");
            properties.setProperty("TextboxRunWithoutTags", Settings.getInstance().tbWithoutTags);
            properties.setProperty("TextboxRunWithTags", Settings.getInstance().tbWithTags);
            properties.store(new FileOutputStream(saveWhere), "This is a Snowride project file.");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
