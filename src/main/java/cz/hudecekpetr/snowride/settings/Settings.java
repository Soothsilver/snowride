package cz.hudecekpetr.snowride.settings;

import java.util.ArrayList;
import java.util.List;

public class Settings {
    private static Settings instance;

    public String lastOpenedProject;
    public List<String> lastOpenedProjects = new ArrayList<String>();

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

    public void save() {
        // TODO serialize
    }
}
