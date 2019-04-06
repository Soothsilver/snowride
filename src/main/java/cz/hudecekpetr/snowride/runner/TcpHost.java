package cz.hudecekpetr.snowride.runner;

import com.jsoniter.JsonIterator;
import com.jsoniter.any.Any;
import cz.hudecekpetr.snowride.Extensions;
import cz.hudecekpetr.snowride.tree.Scenario;
import cz.hudecekpetr.snowride.ui.DeferredActions;
import cz.hudecekpetr.snowride.ui.Images;
import cz.hudecekpetr.snowride.ui.MainForm;
import javafx.application.Platform;
import org.apache.commons.io.Charsets;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayDeque;
import java.util.Map;
import java.util.Queue;

public class TcpHost {
    public int portNumber = 63222;
    private ServerSocket serverSocket = null;
    private MainForm mainForm;
    private RunTab runTab;

    public TcpHost(RunTab runTab,MainForm mainForm) {
        this.runTab = runTab;

        this.mainForm = mainForm;
    }

    public void start() {
        while (serverSocket == null) {
            try {
                serverSocket = new ServerSocket(portNumber);
            } catch (IOException exception) {
                portNumber++;
                serverSocket = null;
            }
        }
        Thread thread = new Thread(() -> {
            while (true) {
                try {
                    Socket client = serverSocket.accept();
                    Thread listenToClient = new Thread(() -> {
                        listenToClient(client);
                    });
                    listenToClient.setDaemon(true);
                    listenToClient.start();
                } catch (IOException e) {
                    // ignore that client.
                }
            }
        });
        thread.setDaemon(true);
        thread.start();
    }

    private void listenToClient(Socket client) {
        Queue<String> incomingDataBuffer = new ArrayDeque<>();
        StringBuilder incomingBuffer = new StringBuilder();
        int buffersize = 4096;
        char[] buffer = new char[buffersize];
        Thread t1 = new Thread(() ->
        {
            try {
                while (true) {
                    String readFrom = null;
                    synchronized (incomingDataBuffer) {
                        while (incomingDataBuffer.size() == 0) {
                            incomingDataBuffer.wait();
                        }
                        readFrom = incomingDataBuffer.poll();
                    }
                    if (readFrom.equals("POISON")) {
                        // terminate
                        return;
                    } else {
                        incomingBuffer.append(readFrom);
                        analyzeBuffer(incomingBuffer);
                    }
                }
            } catch (Exception ex) {
                logIntoMainOutput("TCP execution exception: " + Extensions.toStringWithTrace(ex));
            }
        });
        t1.setDaemon(true);
        t1.start();
        try {
            InputStream inputStream = client.getInputStream();
            InputStreamReader reader = new InputStreamReader(inputStream, StandardCharsets.US_ASCII);
            while (true) {
                int actualSize = reader.read(buffer, 0, buffersize);
                if (actualSize <= 0) {
                    synchronized (incomingDataBuffer) {
                        incomingDataBuffer.add("POISON");
                        incomingDataBuffer.notifyAll();
                    }
                    logIntoMainOutput("[TCP Server] Connection to Robot listener closed normally.");
                    return;
                }
                String whatArrived = new String(buffer, 0, actualSize);
                synchronized (incomingDataBuffer) {
                    incomingDataBuffer.add(whatArrived);
                    incomingDataBuffer.notifyAll();
                }
            }
        } catch (Exception exception) {
            synchronized (incomingDataBuffer) {
                incomingDataBuffer.add("POISON");
                incomingDataBuffer.notifyAll();
            }
            logIntoMainOutput("[TCP Server] Connection to Robot listener closed abnormally: " + Extensions.toStringWithTrace(exception));
        }
    }

    private void analyzeBuffer(StringBuilder incomingBuffer) {
        String start = incomingBuffer.substring(0, Math.min(incomingBuffer.length(), 20));
        if (start.length() > 0 && start.charAt(0) == 'J') {
            int pipeIndex = start.indexOf('|');
            if (pipeIndex != -1) {
                String theNumber = start.substring(1, pipeIndex);
                int number = Integer.parseInt(theNumber);
                if (incomingBuffer.length() >= pipeIndex + 1 + number) {
                    String data = incomingBuffer.substring(pipeIndex + 1, pipeIndex + 1 + number);
                    performJsonCommand(data);
                    incomingBuffer.delete(0, pipeIndex + 1 + number);
                    analyzeBuffer(incomingBuffer);
                }
            }
        }
    }

    private void performJsonCommand(String data) {
        Any deserialize = JsonIterator.deserialize(data);
        Any arguments = deserialize.get(1);

        schedule(() -> {
            String command = deserialize.get(0).toString();
            switch (command) {
                case "pid":
                    Any pid = arguments.get(0);
                    runTab.run.stoppableProcessId.set(pid.toInt());
                    logIntoLogOutput("Stoppable process PID is: " + runTab.run.stoppableProcessId.getValue());
                    break;
                case "log_message":
                    Map<String, Any> additionals = arguments.get(0).asMap();
                    logIntoLogOutput(additionals.get("timestamp") + " [" + additionals.get("level") + "] " + additionals.get("message"));
                    break;
                case "start_keyword":
                    String keywordName = arguments.get(0).as(String.class);
                    runTab.run.keywordStack.push(keywordName);
                    runTab.run.lastKeywordBeganWhen = System.currentTimeMillis();
                    runTab.lblKeyword.setText(runTab.run.keywordStackAsString());
                    break;
                case "end_keyword":
                    runTab.run.keywordStack.pop();
                    runTab.run.lastKeywordBeganWhen = System.currentTimeMillis();
                    runTab.lblKeyword.setText(runTab.run.keywordStackAsString());
                    break;
                case "start_test":
                    Map<String,Any> auxiliaries = arguments.get(1).asMap();
                    String longname = auxiliaries.get("longname").as(String.class);
                    mainForm.findTestByFullyQualifiedName(longname).imageView.setImage(Images.running);
                    break;
                case "end_test":
                    Map<String,Any> auxiliaries2 = arguments.get(1).asMap();
                    String status = auxiliaries2.get("status").as(String.class);
                    String longname2 = auxiliaries2.get("longname").as(String.class);
                    Scenario endingTest = mainForm.findTestByFullyQualifiedName(longname2);
                    if (status.equals("PASS")){
                        runTab.run.countPassedTests++;
                        endingTest.markTestStatus(TestResult.PASSED);
                    } else {
                        runTab.run.countFailedTests++;
                        endingTest.markTestStatus(TestResult.FAILED);
                    }
                    runTab.updateResultsPanel();
                    break;
                case "log_file":
                    runTab.run.logFile.set(arguments.get(0).as(String.class));
                case "report_file":
                    runTab.run.reportFile.set(arguments.get(0).as(String.class));
                    break;
                case "start_suite":
                case "end_suite":
                case "port":
                case "close":
                    // ignore
                    break;
                default:
                    logIntoLogOutput("Method: " + command + ", arguments: " + arguments.toString());
            }
        });
    }

    private void schedule(Runnable action) {
        DeferredActions.runLater(action);
    }

    private void logIntoLogOutput(String s) {
        DeferredActions.logLater(s + "\n");
    }

    private void logIntoMainOutput(String s) {
        Platform.runLater(() -> {
            runTab.appendGreenText(s);
        });
    }
}
