package net.siudek.media.rename;

import java.nio.file.Path;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import net.siudek.media.CommandsListener;
import net.siudek.media.MediaCommands;

@RequiredArgsConstructor
@Component
public class Generic1RenameStrategy implements RenameStrategy {

    private final CommandsListener commandsListener;

    /// rename yyyyMMdd_hhmmss.* to yyyyMMdd-hhmmss.*
    @Override
    public boolean tryRename(Path value) {
        if (!value.getFileName().toString().matches("\\d{8}_\\d{6}.*")) {
            return false;
        }

        var fileName = value.getFileName().toString();
        var datePart = fileName.substring(0, 8);
        var timePart = fileName.substring(9, 15);
        var restPart = fileName.substring(15);
        var newFileName = String.format("%s-%s%s", datePart, timePart, restPart);
        var cmd = new MediaCommands.RenameMediaItem(value, newFileName);
        commandsListener.on(cmd);
        return true;
    }

}
