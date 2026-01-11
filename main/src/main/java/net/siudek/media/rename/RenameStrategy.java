package net.siudek.media.rename;

import java.nio.file.Path;

import net.siudek.media.CommandsListener;

public interface RenameStrategy {
    
    /// Try to rename the given file according to specific strategy.
    boolean tryRename(Path value);

}
