package net.siudek.media.rename;

import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Optional;

import org.springframework.stereotype.Component;

import net.siudek.media.MediaCommands;

@Component
public class GenericDateOnlyRenameStrategy implements RenameStrategy {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");

    /// normalize yyyyMMdd.* to yyyyMMdd.*
    @Override
    public Optional<MediaCommands> tryRename(Path value) {
        var fileName = value.getFileName().toString();

        if (!fileName.matches("\\d{8}\\..+")) {
            return Optional.empty();
        }

        var datePart = fileName.substring(0, 8);

        try {
            var date = LocalDate.parse(datePart, DATE_FORMATTER);
            var extension = fileName.substring(9);
            var meta = new MediaCommands.GenericMetaYMD(date, extension, value);
            var cmd = new MediaCommands.RenameMediaItem(value, meta);
            return Optional.of(cmd);
        } catch (DateTimeParseException e) {
            return Optional.empty();
        }
    }
}