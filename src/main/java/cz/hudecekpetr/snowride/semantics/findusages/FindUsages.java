package cz.hudecekpetr.snowride.semantics.findusages;

import cz.hudecekpetr.snowride.lexer.LogicalLine;
import cz.hudecekpetr.snowride.semantics.IKnownKeyword;
import cz.hudecekpetr.snowride.tree.HighElement;
import cz.hudecekpetr.snowride.tree.Scenario;
import cz.hudecekpetr.snowride.tree.Suite;
import cz.hudecekpetr.snowride.tree.UltimateRoot;
import cz.hudecekpetr.snowride.ui.Images;
import cz.hudecekpetr.snowride.ui.MainForm;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.MenuItem;
import javafx.scene.image.ImageView;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


public class FindUsages {

    public static List<Scenario> findUsagesAsTestCases(IKnownKeyword needleAsKeyword, Scenario needleAsScenario, UltimateRoot root) {
        return findUsagesInternal(needleAsKeyword, needleAsScenario, root).stream().filter(usg -> usg.getElement() instanceof Scenario && ((Scenario) usg.getElement()).isTestCase())
                .map(usg -> (Scenario)usg.getElement()).distinct().collect(Collectors.toList());
    }
    private static List<Usage> findUsagesInternal(IKnownKeyword needleAsKeyword, Scenario needleAsScenario, UltimateRoot root) {
        List<Usage> usages = new ArrayList<>();
        List<HighElement> allTestsAndKeywords = root.selfAndDescendantHighElements().collect(Collectors.toList());
        for (HighElement he : allTestsAndKeywords) {
            if (he instanceof Suite) {
                Suite suite = ((Suite) he);
                suite.reparseAndRecalculateResources();
            }
            if (he instanceof Scenario) {
                Scenario asScenario = ((Scenario) he);
                ObservableList<LogicalLine> lines = asScenario.getLines();
                for (LogicalLine line : lines) {
                    if (line.cells.size() >= 2) {
                        line.cells.get(line.cells.size() - 1).updateSemanticsStatus();
                        IKnownKeyword keywordInThisCell = line.cells.get(line.cells.size() - 1).keywordOfThisLine;
                        if (keywordInThisCell != null && ((keywordInThisCell == needleAsKeyword && needleAsKeyword != null) || (keywordInThisCell.getScenarioIfPossible() == needleAsScenario && needleAsScenario != null))) {
                            String text = he.getShortName() + ":" + (line.lineNumber.intValue() + 1) + " — " + StringUtils.join(line.cells.stream().map(cell -> cell.contents).iterator(), " ");
                            usages.add(new Usage(text, he));
                        }
                    }
                }
            }
        }
        return usages;
    }
    public static List<MenuItem> findUsages(IKnownKeyword needleAsKeyword, Scenario needleAsScenario, UltimateRoot root) {
        List<MenuItem> menuItems = new ArrayList<>();
        List<Usage> usages = findUsagesInternal(needleAsKeyword, needleAsScenario, root);
        for (Usage usage : usages) {
            if (usage.getElement() instanceof Scenario) {
                Scenario asScenario = ((Scenario) usage.getElement());
                ImageView icon;
                if (asScenario.isTestCase()) {
                    icon = new ImageView(Images.testIcon);
                } else {
                    icon = new ImageView(Images.keywordIcon);
                }
                MenuItem item = new MenuItem(usage.getText(), icon);
                item.setOnAction(new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent event) {
                        MainForm.INSTANCE.selectProgrammaticallyAndRememberInHistory(usage.getElement());
                    }
                });
                if (menuItems.size() >= 100) {
                    MenuItem disabledItem = new MenuItem("...there are more usages");
                    disabledItem.setDisable(true);
                    menuItems.add(disabledItem);
                    return menuItems;
                } else {
                    menuItems.add(item);
                }
            }
        }
        return menuItems;
    }
}
