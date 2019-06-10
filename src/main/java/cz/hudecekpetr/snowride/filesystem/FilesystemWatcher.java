package cz.hudecekpetr.snowride.filesystem;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import cz.hudecekpetr.snowride.Extensions;
import javafx.application.Platform;

import java.io.File;
import java.io.IOException;
import java.nio.file.ClosedWatchServiceException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.concurrent.TimeUnit;

import static java.nio.file.StandardWatchEventKinds.*;

public class FilesystemWatcher {
    private static FilesystemWatcher watcher;
    private WatchService watchService;
    private Cache<Path, Path> recentlyChangedFiles = CacheBuilder.newBuilder()
            .expireAfterWrite(5, TimeUnit.SECONDS)
            .build();
    public static FilesystemWatcher getInstance() {
        if (watcher == null) {
            watcher = new FilesystemWatcher();
        }
        return watcher;
    }

    private FilesystemWatcher() {
        createTheWatchService();
    }

    private boolean shouldIgnoreFilesystemChangesToFile(File filename, boolean isNormalFile, WatchEvent.Kind<?> kind) {
        if (isNormalFile && !Extensions.hasLegalExtension(filename)) {
            return true;
        }
        if (!isNormalFile && kind == ENTRY_MODIFY) {
            // This is just propagation of changes further down the directory tree. No need to react to this.
            return true;
        }
        if (recentlyChangedFiles.getIfPresent(filename.toPath()) != null) {
            // It was Snowride who changed the file.
            return true;
        }
        return false;
    }

    @SuppressWarnings("unchecked")
    private void createTheWatchService() {
        try {
            watchService = FileSystems.getDefault().newWatchService();
            System.out.println("[Watch " + watchService + "] Created.");
            Thread thread = new Thread(() -> {
                try {
                    while (true) {
                        WatchKey key;
                        key = watchService.take();
                        for (WatchEvent<?> event : key.pollEvents()) {
                            WatchEvent.Kind<?> kind = event.kind();
                            if (kind == OVERFLOW) {
                                continue;
                            }
                            WatchEvent<Path> ev = (WatchEvent<Path>) event;
                            Path filename = ev.context();
                            Path changedDirectory = (Path) key.watchable();
                            Path absolutePathToChangedFile = changedDirectory.resolve(filename);
                            File asFile = absolutePathToChangedFile.toFile();
                            boolean isNormalFile = asFile.isFile();
                            if (!shouldIgnoreFilesystemChangesToFile(asFile, isNormalFile, kind)) {
                                boolean isInitRobot = asFile.getName().contains("__init__");
                                Path reloadWhat = whatToReload(absolutePathToChangedFile, changedDirectory, kind, isNormalFile, isInitRobot);
                                Platform.runLater(() -> {
                                    ReloadChangesWindow.activateWindowIfNotActive().addPath(reloadWhat);
                                });
                            }
                        }
                        key.reset();
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (ClosedWatchServiceException e) {
                    // That's fine and expected.
                    System.out.println("[Watch " + watchService + "] Closed.");
                }
            });
            thread.setDaemon(true);
            thread.start();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private Path whatToReload(Path modifiedFile, Path itsParent, WatchEvent.Kind<?> kind, boolean isNormalFile, boolean isInitRobot) {
        if (isNormalFile && !isInitRobot && kind == ENTRY_MODIFY) {
            return modifiedFile;
        } else {
            return itsParent;
        }
    }

    public void forgetEverything() {
        try {
            watchService.close();
        } catch (IOException e) {
            // Silently ignore.
        }
        createTheWatchService();
    }

    public void startWatching(File directoryPath) {
        Path asPath = directoryPath.toPath();
        try {
            asPath.register(watchService, ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void ignoreNextChangeOf(Path path) {
        recentlyChangedFiles.put(path, path);
    }
}
