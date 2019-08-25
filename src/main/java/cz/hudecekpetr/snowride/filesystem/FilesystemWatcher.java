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

/**
 * When something changes in the filesystem, this will pop up an alert asking the user to reload.
 *
 * Not thread safe. Methods of this class must only be called from the Java FX thread.
 */
public class FilesystemWatcher {
    private static FilesystemWatcher watcher;
    private WatchService watchService;

    /**
     * Maintains a list of ABSOLUTE PATHS of files that were recently changed from within Snowride, including directories.
     * Because it is kind-of unlikely that a file will change on the disk in the same 5 seconds that the user changed it
     * within Snowride, we use this as an indicator, when a file changes, about where the change originated from. A smarter
     * idea would be to load the file and compare the contents but that's more difficult to implement.
     */
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

    @SuppressWarnings("RedundantIfStatement")
    private boolean shouldIgnoreFilesystemChangesToFile(File filename, boolean isNormalFile, WatchEvent.Kind<?> kind) {
        if (isNormalFile && !Extensions.hasLegalExtension(filename)) {
            // This is not a file we would load anyway. It could be a Python file or an XML file, but we don't reload
            // those automatically anyway. I suppose it would be kind of a nice feature to alert the user to those changes
            // as well and reload them, though.
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
            // The watch service has a synchronous API and Oracle recommends that it's run on a separate thread:
            watchService = FileSystems.getDefault().newWatchService();
            Thread thread = new Thread(() -> {
                try {
                    // Copied from Oracle Java tutorial.
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
                                // We're not in Java FX thread here, so:
                                Platform.runLater(() -> ReloadChangesWindow.requireFileReload(reloadWhat));
                            }
                        }
                        key.reset();
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (ClosedWatchServiceException e) {
                    // That's fine and expected. Happens once at the beginning and then each time you reload full project.
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
            // Modify a normal file -> it's changed
            return modifiedFile;
        } else {
            // Modify a __init__.robot file -> directory is changed
            // Create a new file -> directory is changed
            // Delete an existing file -> directory is changed
            // Not possible: Modify a directory (that's filtered out before this)
            return itsParent;
        }
    }

    /**
     * Stops watching the root directory. Called when you reload everything.
     */
    public void forgetEverything() {
        try {
            watchService.close();
        } catch (IOException e) {
            // Silently ignore.
        }
        createTheWatchService();
    }

    /**
     * Adds the given directory to the set of directories that are watched for changes to their files.
     */
    public void startWatching(File directoryPath) {
        Path asPath = directoryPath.toPath();
        try {
            asPath.register(watchService, ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Call this just before you change/create/delete a file from within Snowride. You then have 5 seconds to make the change.
     * Use <b>absolute path</b>.
     */
    public void ignoreNextChangeOf(Path path) {
        recentlyChangedFiles.put(path, path);
    }
}
