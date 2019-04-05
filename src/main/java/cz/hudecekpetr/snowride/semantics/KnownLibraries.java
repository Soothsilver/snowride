package cz.hudecekpetr.snowride.semantics;

import cz.hudecekpetr.snowride.semantics.codecompletion.ExternalLibrary;

import java.util.HashMap;
import java.util.Map;

public class KnownLibraries {
    private static KnownLibraries instance;

    public static KnownLibraries getInstance() {
        if (instance == null) {
            instance = new KnownLibraries();
        }
        return instance;
    }

    public static Map<String, ExternalLibrary> knownLibraries = new HashMap<>();

    public void reloadAllLibraries() {
        knownLibraries.clear();
    }

}
