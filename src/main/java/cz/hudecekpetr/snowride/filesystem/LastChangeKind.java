package cz.hudecekpetr.snowride.filesystem;

import cz.hudecekpetr.snowride.tree.RobotFile;

public enum LastChangeKind {
    /**
     * The user changed this file by editing its text. When it's saved, its text contents should be saved directly.
     */
    TEXT_CHANGED,
    /**
     * The user changed this file using assisted grid editing. When it's saved, it should be serialized from its {@link RobotFile} structure.
     */
    STRUCTURE_CHANGED,
    /**
     * All changes to this file were already saved or there were no changes.
     */
    PRISTINE
}
