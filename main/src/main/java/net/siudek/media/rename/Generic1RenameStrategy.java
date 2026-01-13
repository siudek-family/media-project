package net.siudek.media.rename;

import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Optional;

import org.springframework.stereotype.Component;

import net.siudek.media.MediaCommands;

@Component
public class Generic1RenameStrategy implements RenameStrategy {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    /// rename yyyyMMdd_hhmmss.* to yyyyMMdd-hhmmss.*
    @Override
    public Optional<MediaCommands> tryRename(Path value) {
        var fileName = value.getFileName().toString();
        
        if (!fileName.matches("\\d{8}_\\d{6}.*")) {
            return Optional.empty();
        }

        var dateTimePart = fileName.substring(0, 8) + fileName.substring(9, 15);
        
        try {
            var dateTime = LocalDateTime.parse(dateTimePart, FORMATTER);
            var extension = fileName.substring(16); // including dot
            var meta = new MediaCommands.GenericMeta(dateTime, extension, value);
            var cmd = new MediaCommands.RenameMediaItem(value, meta);
            return Optional.of(cmd);
        } catch (DateTimeParseException e) {
            return Optional.empty();
        }
    }

}
