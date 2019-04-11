package cz.hudecekpetr.snowride.semantics;

import java.util.ArrayList;
import java.util.List;

public class Setting {
    public String key;
    public String firstValue;
    public List<String> values;

    public Setting(String key, List<String> values) {
        this.key = key;
        this.values = values;
        this.firstValue = values.size() > 0 ? values.get(0) : null;
    }
}
