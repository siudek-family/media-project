package net.siudek.media.rename;

import java.nio.file.Path;
import java.util.Optional;
import java.util.regex.Pattern;

import org.springframework.stereotype.Component;

import net.siudek.media.MediaCommands;

@Component
public class AmrRenameStrategyPhone implements RenameStrategy {

    /// Pattern for phone calls with + prefix: 2021-11-14 15-57-45 (phone) John Doe (+48 123 456 789) ↗.amr
    private static final Pattern AMR_INTERNATIONAL_PATTERN = Pattern.compile(
        "\\d{4}-\\d{2}-\\d{2} \\d{2}-\\d{2}-\\d{2} \\(phone\\) (.+?) \\((\\+\\d+(?:\\s\\d+)*)\\) ([↙↗])\\.amr"
    );

    /// Pattern for phone calls without + prefix: 2021-11-14 15-57-45 (phone) John Doe (0048123456789) ↙.amr
    private static final Pattern AMR_LOCAL_PATTERN = Pattern.compile(
        "\\d{4}-\\d{2}-\\d{2} \\d{2}-\\d{2}-\\d{2} \\(phone\\) (.+?) \\((\\d+(?:\\s\\d+)*)\\) ([↙↗])\\.amr"
    );

    /// Pattern for unidentified caller from messenger: 2021-11-14 19-49-35 (phone) 2000 ↙.amr
    private static final Pattern AMR_UNIDENTIFIED_PATTERN = Pattern.compile(
        "\\d{4}-\\d{2}-\\d{2} \\d{2}-\\d{2}-\\d{2} \\(phone\\) (\\d+(?:\\s\\d+)*) ([↙↗])\\.amr"
    );

    /// Pattern for unknown named contact without phone: 2021-11-19 18-02-07 (phone) Nieznany kontakt ↙.amr
    private static final Pattern AMR_NAME_ONLY_PATTERN = Pattern.compile(
        "\\d{4}-\\d{2}-\\d{2} \\d{2}-\\d{2}-\\d{2} \\(phone\\) (.+?) ([↙↗])\\.amr"
    );

    /// name example: 2021-11-14 15-57-45 (phone) John Doe (+48 123 456 789) ↗.amr or (0048123456789) ↙.amr or 2000 ↙.amr
    /// Returns Optional containing MediaCommands if pattern matches, empty Optional otherwise
    @Override
    public Optional<MediaCommands> tryRename(Path value) {

        var fileName = value.getFileName().toString();
        
        // Try international pattern first (with contact name and + phone)
        var matcher = AMR_INTERNATIONAL_PATTERN.matcher(fileName);
        String contactName;
        String contactPhone;
        String arrow;
        
        if (matcher.matches()) {
            contactName = matcher.group(1);
            contactPhone = matcher.group(2);
            arrow = matcher.group(3);
        } else {
            // Try local pattern (with contact name and local phone)
            matcher = AMR_LOCAL_PATTERN.matcher(fileName);
            if (matcher.matches()) {
                contactName = matcher.group(1);
                contactPhone = matcher.group(2);
                arrow = matcher.group(3);
            } else {
                // Try unknown named contact without phone
                matcher = AMR_NAME_ONLY_PATTERN.matcher(fileName);
                if (matcher.matches()) {
                    contactName = matcher.group(1);
                    contactPhone = matcher.group(1);
                    arrow = matcher.group(2);
                } else {
                    // Try unidentified caller pattern (just ID number)
                    matcher = AMR_UNIDENTIFIED_PATTERN.matcher(fileName);
                    if (!matcher.matches()) {
                        return Optional.empty();
                    }
                    contactName = matcher.group(1);
                    contactPhone = matcher.group(1);  // Use ID as phone for unidentified
                    arrow = matcher.group(2);
                }
            }
        }
        
        // Parse date and time from the filename
        var dateTime = AmrDateTimeParser.parseDateTime(fileName);
        
        // Determine call direction based on arrow
        var direction = switch(arrow) {
            case "↙" -> MediaCommands.CallDirection.INCOMING;
            case "↗" -> MediaCommands.CallDirection.OUTGOING;
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
