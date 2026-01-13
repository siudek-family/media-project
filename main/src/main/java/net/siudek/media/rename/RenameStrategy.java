package net.siudek.media.rename;

import java.nio.file.Path;

public interface RenameStrategy {
    
    /// Try to rename the given file according to specific strategy.
    boolean tryRename(Path value);

}
