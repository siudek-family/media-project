package net.siudek.media;

import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/// Defines all possible commands emitted by Media related to media assets.
/// Such commands, when stored, can be executed later on media assets.
public sealed interface MediaCommands {

    sealed interface Meta { }

    record GenericMeta(LocalDateTime date, String extension, Path location) implements Meta {}
    
    sealed interface AmrDetails {}
    record PhoneCall(String contactName, String contactPhone, String direction) implements AmrDetails {}
    record MicRecording(String title) implements AmrDetails {}

    record AmrMeta(String datePart, String timePart, AmrDetails amrDetails, Path location) implements Meta {}

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
            case AmrMeta amrMeta -> switch (amrMeta.amrDetails()) {
                case PhoneCall phoneCall -> {
                    var direction = switch (phoneCall.direction()) {
                        case "incoming" -> "↘";
                        case "outcoming" -> "↗";
                        default -> throw new IllegalStateException("Unexpected value: " + phoneCall.direction());
                    };
                    yield String.format("%s-%s (%s) (%s) %s.amr",
                        amrMeta.datePart(),
                        amrMeta.timePart(),
                        phoneCall.contactName(),
                        phoneCall.contactPhone(),
                        direction);
                }
                case MicRecording micRecording -> {
                    yield String.format("%s-%s (mic) %s.amr",
                        amrMeta.datePart(),
                        amrMeta.timePart(),
                        micRecording.title());
                }
                case null -> throw new IllegalArgumentException("AmrDetails cannot be null");
            };
            case null -> throw new IllegalArgumentException("Meta cannot be null");
        };
    }

}