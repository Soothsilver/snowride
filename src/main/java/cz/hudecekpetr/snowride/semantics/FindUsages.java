package cz.hudecekpetr.snowride.semantics;

import cz.hudecekpetr.snowride.lexer.LogicalLine;
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


public class FindUsages {
    public static List<MenuItem> findUsages(Scenario keyword, UltimateRoot root) {
        List<MenuItem> menuItems = new ArrayList<>();
        root.selfAndDescendantHighElements().forEachOrdered(he -> {
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
                        if (keywordInThisCell != null && keywordInThisCell.getScenarioIfPossible() == keyword) {
                            String text = he.getShortName() + ":" + (line.lineNumber.intValue() + 1) + " — " + StringUtils.join(line.cells.stream().map(cell -> cell.contents).iterator(), " ");
                            System.out.println(text);
                            ImageView icon;
                            if (((Scenario) he).isTestCase()) {
                                icon = new ImageView(Images.testIcon);
                            } else {
                                icon = new ImageView(Images.keywordIcon);
                            }
                            MenuItem item = new MenuItem(text, icon);
                            item.setOnAction(new EventHandler<ActionEvent>() {
                                @Override
                                public void handle(ActionEvent event) {
                                    MainForm.INSTANCE.selectProgrammaticallyAndRememberInHistory(he);
                                }
                            });
                            menuItems.add(item);
                        }
                    }
                }
            }
        });
        return menuItems;
    }
}
