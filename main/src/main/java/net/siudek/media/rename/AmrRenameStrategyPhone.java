package net.siudek.media.rename;

import java.nio.file.Path;
import java.util.Optional;
import java.util.regex.Pattern;

import org.springframework.stereotype.Component;

import net.siudek.media.MediaCommands;

@Component
public class AmrRenameStrategyPhone implements RenameStrategy {

    /// Example patterns to match:
    /// (phone) John Doe (+XX XXX XXX XXX) ↗.amr 
    /// (phone) John Doe (0048123456789) ↘.amr
    private static final Pattern AMR_PATTERN = Pattern.compile(
        "\\d{4}-\\d{2}-\\d{2} \\d{2}-\\d{2}-\\d{2} \\(phone\\) (.+?) \\((\\+?\\d+(?:\\s\\d+)*)\\) ([↗↘])\\.amr"
    );

    /// name example: 2021-11-14 15-57-45 (phone) John Doe (+48 123 456 789) ↗.amr
    /// Returns Optional containing MediaCommands if pattern matches, empty Optional otherwise
    @Override
    public Optional<MediaCommands> tryRename(Path value) {

        var fileName = value.getFileName().toString();
        var matcher = AMR_PATTERN.matcher(fileName);
        
        if (!matcher.matches()) {
            return Optional.empty();
        }
        
        // Parse date and time from the filename
        var dateTime = AmrDateTimeParser.parseDateTime(fileName);

        // Extract contact name and phone from regex groups
        var contactName = matcher.group(1);
        var contactPhone = matcher.group(2);
        var arrow = matcher.group(3);
        
        // Determine call direction based on arrow
        var direction = switch(arrow) {
            case "↘" -> "incoming";
            case "↗" -> "outcoming";
            default -> throw new IllegalStateException("Unexpected value: " + arrow);
        };

        // Create meta from available parts
        var meta = new MediaCommands.AmrPhoneCallMeta(
            dateTime,
            contactName,
            contactPhone,
            direction,
            value);
        
        var cmd = new MediaCommands.RenameMediaItem(value, meta);
        return Optional.of(cmd);
    }

}
