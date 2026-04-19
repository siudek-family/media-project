package net.siudek.media.rename;

import java.nio.file.Path;
import java.util.Optional;

import net.siudek.media.MediaCommands;

/// Naming of the media files should be normalized so that they can be processed in a simpler way
public interface RenameStrategy {
    
    /// Try to rename the given file according to specific strategy.
    /// Returns Optional containing MediaCommands if the strategy matches, empty Optional otherwise.
    Optional<MediaCommands> tryRename(Path value);

}
