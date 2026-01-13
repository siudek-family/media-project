package net.siudek.media.rename;

import java.nio.file.Path;
import java.util.regex.Pattern;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import net.siudek.media.CommandsListener;
import net.siudek.media.MediaCommands;

@RequiredArgsConstructor
@Component
public class Amr1RenameStrategy implements RenameStrategy {

    private final CommandsListener commandsListener;

    // Pattern to match: (phone) Contact Name (+XX XXX XXX XXX) ↗.amr or ↘.amr
    private static final Pattern AMR_PATTERN = Pattern.compile(
        "\\d{4}-\\d{2}-\\d{2} \\d{2}-\\d{2}-\\d{2} \\(phone\\) (.+?) \\((.+?)\\) ([↗↘])\\.amr"
    );

    /// name example: 2021-11-14 15-57-45 (phone) John Doe (+48 123 456 789) ↗.amr
    /// return true if name can be converted, false otherwise
    @Override
    public boolean tryRename(Path value) {

        var fileName = value.getFileName().toString();
        var matcher = AMR_PATTERN.matcher(fileName);
        
        if (!matcher.matches()) {
            return false;
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
        commandsListener.on(cmd);
        return true;
    }

}
