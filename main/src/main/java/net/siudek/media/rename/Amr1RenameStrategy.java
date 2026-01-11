package net.siudek.media.rename;

import java.nio.file.Path;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import net.siudek.media.CommandsListener;
import net.siudek.media.MediaCommands;

@RequiredArgsConstructor
@Component
public class Amr1RenameStrategy implements RenameStrategy {

    private final CommandsListener commandsListener;

    /// name example: 2021-11-14 15-57-45 (phone) Iza Kapała (+48 503 594 583) ↗.amr
    /// should be renamed to: 20211114-155745.outcoming.2021-11-14 15-57-45 (phone) Iza Kapała (+48 503 594 583) ↗.amr
    /// return true if name can be converted, false otherwise
    @Override
    public boolean tryRename(Path value) {

        // Check if the filename matches the expected pattern: YYYY-MM-DD HH-MM-SS rest.amr
        if (!value.getFileName().toString().matches("\\d{4}-\\d{2}-\\d{2} \\d{2}-\\d{2}-\\d{2} .*")) {
            return false;
        }
        
        // Extract the name from the file path
        var fileName = value.getFileName().toString();
        
        // Extract the date and time parts from the filename
        // Format: YYYY-MM-DD HH-MM-SS
        var datePart = fileName.substring(0, 10).replace("-", ""); // yyyyMMdd
        var timePart = fileName.substring(11, 19).replace("-", ""); // hhmmss
        
        // Extract everything after the time (excluding the leading space, keeping the rest of the original part)
        var restPart = fileName.substring(19);  // This includes " (phone) Iza ... " 
        
        // Create the new filename: yyyyMMdd-hhmmss.outcoming.{original-date-time restPart}
        var newFileName = String.format("%s-%s.outcoming.%s%s", datePart, timePart, fileName.substring(0, 19), restPart);
        
        // Emit command to rename the file
        var cmd = new MediaCommands.RenameMediaItem(value, newFileName);
        commandsListener.on(cmd);
        return true;
    }

}
