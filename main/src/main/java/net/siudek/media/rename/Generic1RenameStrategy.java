package net.siudek.media.rename;

import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import net.siudek.media.CommandsListener;
import net.siudek.media.MediaCommands;

@RequiredArgsConstructor
@Component
public class Generic1RenameStrategy implements RenameStrategy {

    private final CommandsListener commandsListener;
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    /// rename yyyyMMdd_hhmmss.* to yyyyMMdd-hhmmss.*
    @Override
    public boolean tryRename(Path value) {
        var fileName = value.getFileName().toString();
        
        if (!fileName.matches("\\d{8}_\\d{6}.*")) {
            return false;
        }

        var dateTimePart = fileName.substring(0, 8) + fileName.substring(9, 15);
        
        try {
            var dateTime = LocalDateTime.parse(dateTimePart, FORMATTER);
            var extension = fileName.substring(15);
            var meta = new MediaCommands.GenericMeta(dateTime, extension, value);
            var cmd = new MediaCommands.RenameMediaItem(value, meta);
            commandsListener.on(cmd);
            return true;
        } catch (DateTimeParseException e) {
            return false;
        }
    }

}
