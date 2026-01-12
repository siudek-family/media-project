package net.siudek.media;

import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/// Defines all possible commands emitted by Media related to media assets.
/// Such commands, when stored, can be executed later on media assets.
public sealed interface MediaCommands {

    sealed interface Meta { }

    record GenericMeta(LocalDateTime date, String extension, Path location) implements Meta {}
    
    record AmrMeta(String datePart, String timePart, String contactName, String contancPhone, String direction, Path location) implements Meta {}

    /// Rename media file to the new name without changing its location.
    record RenameMediaItem(Path from, Meta meta) implements MediaCommands {}

    /// creates a filename based on various attributed of the media file
    static String asFilename(Meta meta) {
        var formatter = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss");
        return switch (meta) {
            case GenericMeta genericMeta -> {
                var date = genericMeta.date();
                yield date.format(formatter) + "." + genericMeta.extension();
            }
            case AmrMeta amrMeta -> String.format("%s.%s.%s.amr",
                    amrMeta.direction().toLowerCase(),
                    amrMeta.contactName().replaceAll(" ", "_"),
                    amrMeta.contancPhone().replaceAll("[ +()-]", ""));
            case null -> throw new IllegalArgumentException("Meta cannot be null");
        };
    }

}