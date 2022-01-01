package cz.hudecekpetr.snowride.filesystem

import cz.hudecekpetr.snowride.tree.highelements.FileSuite
import cz.hudecekpetr.snowride.tree.highelements.FolderSuite
import cz.hudecekpetr.snowride.tree.highelements.HighElement
import cz.hudecekpetr.snowride.ui.MainForm
import java.io.File
import java.nio.file.StandardWatchEventKinds.ENTRY_CREATE
import java.nio.file.StandardWatchEventKinds.ENTRY_DELETE
import java.nio.file.WatchEvent

object FilesystemWatcherUtils {
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