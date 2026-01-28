package net.siudek.media.rename;

import net.siudek.media.MediaCommands;
import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.time.Year;
import java.util.Optional;

/// Rename strategy for files in Rodzinne/yyyy folder structure.
/// Matches files that:
/// - Are directly in a folder matching pattern: Rodzinne/yyyy (case-insensitive)
/// - Have filenames that don't start with a digit
/// Emits RenameMediaItem with GenericMetaYear metadata.
@Component
public class FamilyYearRenameStrategy implements RenameStrategy {

    @Override
    public Optional<MediaCommands> tryRename(Path value) {
        var parent = value.getParent();
        if (parent == null) {
            return Optional.empty();
        }

        var grandparent = parent.getParent();
        if (grandparent == null) {
            return Optional.empty();
        }

        // Check if grandparent folder is "Rodzinne" (case-insensitive)
        var grandparentName = grandparent.getFileName().toString();
        if (!grandparentName.equalsIgnoreCase("Rodzinne")) {
            return Optional.empty();
        }

        // Check if parent folder matches yyyy pattern
        var parentName = parent.getFileName().toString();
        if (!parentName.matches("\\d{4}")) {
            return Optional.empty();
        }

        // Get filename and check if it starts with a digit
        var fileName = value.getFileName().toString();
        if (fileName.isEmpty() || Character.isDigit(fileName.charAt(0))) {
            return Optional.empty();
        }

        // Extract extension
        var lastDotIndex = fileName.lastIndexOf('.');
        if (lastDotIndex == -1) {
            return Optional.empty();
        }

        var extension = fileName.substring(lastDotIndex + 1);
        var content = fileName.substring(0, lastDotIndex);

        // Parse year from parent folder name
        try {
            var year = Year.of(Integer.parseInt(parentName));
            var meta = new MediaCommands.GenericMetaYear(year, content, extension, value);
            var cmd = new MediaCommands.RenameMediaItem(value, meta);
            return Optional.of(cmd);
        } catch (NumberFormatException e) {
            return Optional.empty();
        }
    }
}
