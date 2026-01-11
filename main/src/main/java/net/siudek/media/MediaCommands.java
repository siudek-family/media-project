package net.siudek.media;

import java.nio.file.Path;

/// Defines all possible commands emitted by Media related to media assets.
/// Such commands, when stored, can be executed later on media assets.
public sealed interface MediaCommands {

    /// Rename media file to the new name without changing its location.
    record RenameMediaItem(Path from, String newName) implements MediaCommands {}
}