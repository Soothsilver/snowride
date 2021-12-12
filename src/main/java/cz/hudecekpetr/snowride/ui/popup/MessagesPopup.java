package cz.hudecekpetr.snowride.ui.popup;

import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.TextFlow;
import org.fxmisc.flowless.VirtualizedScrollPane;
import org.fxmisc.richtext.StyledTextArea;
import org.fxmisc.richtext.TextExt;
import org.fxmisc.richtext.model.SimpleEditableStyledDocument;
import org.robotframework.jaxb.*;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static cz.hudecekpetr.snowride.runner.RunTab.INITIAL_STYLE;


public class MessagesPopup extends SnowPopup {

    private final Label title;
    private final VBox messagesPane;
    private final StyledTextArea<String, String> textArea;

    public MessagesPopup() {
        this.setAutoHide(true);
        this.setAnchorLocation(AnchorLocation.CONTENT_TOP_LEFT);
        title = new Label("No Messages");
        title.setStyle("-fx-font-size: 12pt; -fx-font-weight: bold;");

        textArea = new StyledTextArea<>("", TextFlow::setStyle,
                INITIAL_STYLE, TextExt::setStyle,
                new SimpleEditableStyledDocument<>("", INITIAL_STYLE),
                true);
        textArea.setUseInitialStyleForInsertion(true);
        textArea.setEditable(false);
        textArea.setPadding(new Insets(4));
        textArea.setStyle("-fx-background-color: -fx-control-inner-background");
        VirtualizedScrollPane<StyledTextArea<String, String>> scrollPane = new VirtualizedScrollPane<>(textArea);


        messagesPane = new VBox(title, scrollPane);
        VBox.setVgrow(scrollPane, Priority.ALWAYS);
        messagesPane.setStyle("-fx-background-color: whitesmoke;");
        messagesPane.setPrefWidth(450);
        messagesPane.setPrefHeight(450);
        messagesPane.setPadding(new Insets(6));

        this.setConsumeAutoHidingEvents(false);
        this.getContent().add(messagesPane);
    }

    public void setArgs(List<String> args) {
        clear();
        title.setText("Arguments");

        for (String argument : args) {
            textArea.appendText(argument + System.lineSeparator() + System.lineSeparator());
        }
    }

    public void setIterations(List<ForIteration> iterations) {
        clear();
        title.setText("FOR iterations");

        int totalLength = 0;
        int maxWidth = 0;
        for (int i = 0; i < iterations.size(); i++) {
            ForIteration iteration = iterations.get(i);
            if (!iteration.getVars().isEmpty()) {
                String iterationInfo = "[FOR_ITERATION_" + i + "]";
                String text = iterationInfo + " " + iteration.getVars().stream().map(ForIterationVariable::getValue).collect(Collectors.joining("    |    "));
                textArea.appendText(text + System.lineSeparator() + System.lineSeparator());
                String color = "lightgray";
                switch (iteration.getStatus().getStatus()) {
                    case NOT_RUN:
                    case SKIP:
                        color = "#dddddd";
                        break;
                    case FAIL:
                        color = "#ce3e01";
                        break;
                    case PASS:
                        color = "#97bd61";
                        break;
                }
                textArea.setStyle(totalLength, totalLength + iterationInfo.length(), "-fx-font-weight: bold; -fx-fill: " + color + ";");
                totalLength += text.length() + 2;
                if (text.length() > maxWidth) {
                    maxWidth = text.length();
                }
            }
        }
        setPaneWidth(maxWidth);
    }

    public void setKeyword(Keyword keyword) {
        if (keyword.getType() == KeywordType.FOR && !keyword.getKwOrForOrIf().isEmpty()) {
            rf3ForIterations(keyword.getKwOrForOrIf().stream().filter(o -> o instanceof Keyword).map(o -> (Keyword) o).collect(Collectors.toList()));
            return;
        }

        clear();
        List<Message> messages = keyword.getMessages();
        if (!messages.isEmpty()) {
            setMessages(messages);
        } else {
            Keyword failingKeyword = keyword.getFailingKeyword();
            if (failingKeyword != null && !failingKeyword.getMessages().isEmpty()) {
                messages = failingKeyword.getMessages();
                setMessages(messages, "Output messages (source: " + failingKeyword.getFullName(keyword) + ")");
            }
        }
    }

    // RobotFramework 3.X
    public void rf3ForIterations(List<Keyword> keywords) {
        clear();
        title.setText("FOR iterations");

        int totalLength = 0;
        int maxWidth = 0;
        for (int i = 0; i < keywords.size(); i++) {
            Keyword iteration = keywords.get(i);
            String iterationInfo = "[FOR_ITERATION_" + i + "]";
            String text = iterationInfo + " " + iteration.getName();
            textArea.appendText(text + System.lineSeparator() + System.lineSeparator());
            String color = "lightgray";
            switch (iteration.getStatus().getStatus()) {
                case NOT_RUN:
                case SKIP:
                    color = "#dddddd";
                    break;
                case FAIL:
                    color = "#ce3e01";
                    break;
                case PASS:
                    color = "#97bd61";
                    break;
            }
            textArea.setStyle(totalLength, totalLength + iterationInfo.length(), "-fx-font-weight: bold; -fx-fill: " + color + ";");
            totalLength += text.length() + 2;
            if (text.length() > maxWidth) {
                maxWidth = text.length();
            }
        }
        setPaneWidth(maxWidth);
    }

    public void setMessages(List<Message> messages) {
        setMessages(messages, "Output messages");
    }

    private void setMessages(List<Message> messages, String titleText) {
        clear();
        if (messages.isEmpty()) {
            return;
        }
        title.setText(titleText);
        int totalLength = 0;
        int maxWidth = 0;
        for (Message message : messages) {
            String level = "[" + message.getLevel().value() + "]";
            String msg = message.getValue().replaceFirst("\n {30}+", " ").replaceAll(" {36}", "     ");
            Optional<String> max = Arrays.stream(msg.split("\n")).max(Comparator.comparingInt(String::length));
            if (max.isPresent() && max.get().length() > maxWidth) {
                maxWidth = max.get().length();
            }
            textArea.appendText(level + " " + msg + System.lineSeparator() + System.lineSeparator());
            String color = "lightgray";
            switch (message.getLevel()) {
                case FAIL:
                case ERROR:
                    color = "#e74c3c";
                    break;
                case WARN:
                    color = "#FFFF8F";
                    break;
            }
            textArea.setStyle(totalLength, totalLength + level.length(), "-fx-font-weight: bold; -fx-fill: " + color + ";");
            totalLength += level.length() + msg.length() + 3;
        }
        setPaneWidth(maxWidth);
        textArea.autosize();
    }

    private void clear() {
        title.setText("No Messages");
        textArea.clear();
        textArea.clearStyle(0);
    }

    private void setPaneWidth(int width) {
        if (width > 0) {
            messagesPane.setPrefWidth(Math.min(width * 10 + 75, 1620));
        }
    }
}
