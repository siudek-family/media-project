package net.siudek.media;

import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/// Defines all possible commands emitted by Media related to media assets.
/// Such commands, when stored, can be executed later on media assets.
public sealed interface MediaCommands {

    sealed interface Meta { }

    record GenericMeta(LocalDateTime date, String extension, Path location) implements Meta {}
    
    record AmrPhoneCallMeta(LocalDateTime dateTime, String contactName, String contactPhone, String direction, Path location) implements Meta {}

    record AmrMicRecordingMeta(LocalDateTime dateTime, String title, Path location) implements Meta {}

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
            case AmrPhoneCallMeta phoneCallMeta -> {
                var direction = switch (phoneCallMeta.direction()) {
                    case "incoming" -> "↘";
                    case "outcoming" -> "↗";
                    default -> throw new IllegalStateException("Unexpected value: " + phoneCallMeta.direction());
                };
                yield String.format("%s (%s) (%s) %s.amr",
                    phoneCallMeta.dateTime().format(formatter),
                    phoneCallMeta.contactName(),
                    phoneCallMeta.contactPhone(),
                    direction);
            }
            case AmrMicRecordingMeta micRecordingMeta -> {
                yield String.format("%s (mic) %s.amr",
                    micRecordingMeta.dateTime().format(formatter),
                    micRecordingMeta.title());
            }
            case null -> throw new IllegalArgumentException("Meta cannot be null");
        };
    }

}