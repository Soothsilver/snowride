package cz.hudecekpetr.snowride.tree;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class TestCase extends HighElement {

    public TestCase(String name, String contents) {
        super(name, contents, new ArrayList<>());
    }

    @Override
    public void saveAll() throws IOException {
        // Saved as part of the file suite.
    }
}
