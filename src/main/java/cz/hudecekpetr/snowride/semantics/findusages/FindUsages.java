package cz.hudecekpetr.snowride.semantics.findusages;

import cz.hudecekpetr.snowride.tree.Cell;
import cz.hudecekpetr.snowride.tree.LogicalLine;
import cz.hudecekpetr.snowride.semantics.IKnownKeyword;
import cz.hudecekpetr.snowride.tree.highelements.HighElement;
import cz.hudecekpetr.snowride.tree.highelements.Scenario;
import cz.hudecekpetr.snowride.tree.highelements.Suite;
import cz.hudecekpetr.snowride.tree.highelements.UltimateRoot;
import cz.hudecekpetr.snowride.ui.MainForm;
import javafx.collections.ObservableList;
import javafx.scene.control.MenuItem;
import javafx.scene.image.ImageView;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


public class FindUsages {

    public static List<Scenario> findUsagesAsTestCases(IKnownKeyword needleAsKeyword, Scenario needleAsScenario, UltimateRoot root) {
        return findUsagesInternal(needleAsKeyword, needleAsScenario, root).stream().filter(usg -> usg.getElement() instanceof Scenario && ((Scenario) usg.getElement()).isTestCase())
                .map(usg -> (Scenario) usg.getElement()).distinct().collect(Collectors.toList());
    }

    public static List<Usage> findUsagesInternal(IKnownKeyword needleAsKeyword, Scenario needleAsScenario, UltimateRoot root) {
        List<Usage> usages = new ArrayList<>();
        List<HighElement> allTestsAndKeywords = root.selfAndDescendantHighElements().collect(Collectors.toList());
        for (HighElement he : allTestsAndKeywords) {
            if (he instanceof Suite) {
                Suite suite = ((Suite) he);
                suite.reparseAndRecalculateResources();
                if (suite.fileParsed != null) {
                    ObservableList<LogicalLine> pairs = suite.fileParsed.findOrCreateSettingsSection().getPairs();
                    for (LogicalLine line : pairs) {
                        findUsagesInLine(needleAsKeyword, needleAsScenario, usages, he, line);
                    }
                }
            }
            if (he instanceof Scenario) {
                Scenario asScenario = ((Scenario) he);
                ObservableList<LogicalLine> lines = asScenario.getLines();
                for (LogicalLine line : lines) {
                    findUsagesInLine(needleAsKeyword, needleAsScenario, usages, he, line);
                }
            }
        }
        return usages;
    }

    private static void findUsagesInLine(IKnownKeyword needleAsKeyword, Scenario needleAsScenario, List<Usage> usages, HighElement he, LogicalLine line) {
        if (line.cells.size() >= 2) {
            line.recalculateSemantics();
            for (Cell cell : line.cells) {
                IKnownKeyword keywordInThisCell = cell.getSemantics().thisHereKeyword;
                if (keywordInThisCell != null && (keywordInThisCell == needleAsKeyword || keywordInThisCell.getScenarioIfPossible() == needleAsScenario && needleAsScenario != null)) {
                    String text = he.getShortName() + ":" + (line.lineNumber.intValue() + 1) + " — " + StringUtils.join(line.cells.stream().map(thaCell -> thaCell.contents).iterator(), " ");
                    usages.add(new Usage(text, line, line.cells.indexOf(cell), he));
                }
            }
        }
    }

    public static List<MenuItem> findUsages(IKnownKeyword needleAsKeyword, Scenario needleAsScenario, UltimateRoot root) {
        List<MenuItem> menuItems = new ArrayList<>();
        List<Usage> usages = findUsagesInternal(needleAsKeyword, needleAsScenario, root);
        for (Usage usage : usages) {
            ImageView icon = new ImageView(usage.getElement().getAutocompleteIcon());
            MenuItem item = new MenuItem(usage.getText(), icon);
            item.setOnAction(event -> MainForm.INSTANCE.selectProgrammaticallyAndRememberInHistory(usage.getElement()));
            if (menuItems.size() >= 100) {
                MenuItem disabledItem = new MenuItem("...there are more usages");
                disabledItem.setDisable(true);
                menuItems.add(disabledItem);
                return menuItems;
            } else {
                menuItems.add(item);
            }

        }
        return menuItems;
    }
}
