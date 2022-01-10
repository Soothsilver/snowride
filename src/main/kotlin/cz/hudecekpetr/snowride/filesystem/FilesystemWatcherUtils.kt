package cz.hudecekpetr.snowride.filesystem

import cz.hudecekpetr.snowride.tree.highelements.FileSuite
import cz.hudecekpetr.snowride.tree.highelements.FolderSuite
import cz.hudecekpetr.snowride.tree.highelements.HighElement
import cz.hudecekpetr.snowride.ui.MainForm
import java.io.File
import java.nio.file.Path
import java.nio.file.StandardWatchEventKinds.ENTRY_CREATE
import java.nio.file.StandardWatchEventKinds.ENTRY_DELETE
import java.nio.file.WatchEvent
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

object FilesystemWatcherUtils {

    private val pollingProcess: ExecutorService = Executors.newSingleThreadExecutor()
    private val watchEvents: MutableMap<Path, MutableList<WatchEvent<*>>> = mutableMapOf()

    /**
     * Workaround for specific implementation of [java.nio.file.WatchService]. When on modification of single file multiple watch events are
     * received, not only ENTRY_MODIFY, but also both ENTRY_CREATE and ENTRY_DELETE. Here we are crunching the events together and removing
     * ENTRY_CREATE/ENTRY_DELETE pairs.
     */
    fun waitToReceiveAllEvents(watchEvent: MutableList<WatchEvent<*>>, changeDirectory: Path, filesystemWatcher: FilesystemWatcher) {
        synchronized(watchEvents) {
            watchEvents.getOrPut(changeDirectory) { mutableListOf() }.addAll(watchEvent)
        }
        pollingProcess.execute {
            Thread.sleep(250)
            val watchEventsToProcess: MutableMap<Path, MutableList<WatchEvent<*>>>
            synchronized(watchEvents) {
                watchEvents.forEach { (_, all) ->
                    val create = all.filter { it.kind() == ENTRY_CREATE }
                    val delete = all.filter { it.kind() == ENTRY_DELETE }
                    all.removeAll(create.filter { c -> delete.any { c.context() == it.context() } })
                    all.removeAll(delete.filter { d -> create.any { d.context() == it.context() } })
                }
                watchEventsToProcess = watchEvents.toMutableMap()
                watchEvents.clear()
            }
            watchEventsToProcess.forEach { (d, e) ->
                filesystemWatcher.processWatchEvents(e, d)
            }
        }
    }

    fun shouldIgnoreFilesystemChangesToFile(reloadWhat: File, kind: WatchEvent.Kind<*>): Boolean {
        // find corresponding 'HighElement' in Snowride
        val highElement = MainForm.INSTANCE.rootElement.childrenRecursively.find { element: HighElement ->
            if (element is FileSuite && element.file == reloadWhat) {
                return@find true
            } else if (element is FolderSuite && (element.initFile == reloadWhat || element.directoryPath == reloadWhat)) {
                return@find true
            }
            false
        }

        // When Snowride does not know about such 'file'
        if (highElement == null) {
            if (kind == ENTRY_CREATE) {
                return false
            }
            // ignore "DELETE"
            // "MODIFY" does not apply here (we would have found corresponding 'HighElement' otherwise)
            return true
        }

        // deletion outside Snowride
        if (kind == ENTRY_DELETE) {
            return false
        }

        // creation of 'directory' from Snowride
        if (reloadWhat.isDirectory && kind == ENTRY_CREATE) {
            return true
        }

        // compare pristine content
        return highElement.pristineContents == reloadWhat.readText()
    }
}