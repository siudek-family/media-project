package net.siudek.media;

import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.Year;
import java.time.format.DateTimeFormatter;

/// Defines all possible commands emitted by Media related to media assets.
/// Such commands, when stored, can be executed later on media assets.
public sealed interface MediaCommands {

    sealed interface Meta { }

    enum CallDirection {
        INCOMING,
        OUTGOING,
        UNDEFINED
    }

    record GenericMeta(LocalDateTime date, String extension, Path location) implements Meta {}
    record GenericMetaYear(Year date, String content, String extension, Path location) implements Meta {}
    
    /// name example: 2021-11-14 15-57-45 (phone) John Doe (+48 123 456 789) ↗.amr
    /// name example: 2021-11-14 15-57-45 (phone) John Doe (+48 123 456 789) .amr
    record AmrPhoneCallMeta(LocalDateTime dateTime, String contactName, String contactPhone, CallDirection direction, Path location) implements Meta {}

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
            case GenericMetaYear genericMetaYear -> {
                yield String.format("%s %s.%s",
                    genericMetaYear.date().getValue(),
                    genericMetaYear.content(),
                    genericMetaYear.extension());
            }
            case AmrPhoneCallMeta phoneCallMeta -> {
                var direction = switch (phoneCallMeta.direction()) {
                    case INCOMING -> " ↘";
                    case OUTGOING -> " ↗";
                    case UNDEFINED -> "";
                };
                yield String.format("%s (%s) (%s)%s.amr",
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