package net.siudek.media.rename;

import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.regex.Pattern;

import org.springframework.stereotype.Component;

import net.siudek.media.MediaCommands;

@Component
/// Example to match: 2021-11-14 17-49-05 (mic) Nagrywanie dyktafonu.amr
/// or: mic_20200801-173827.amr
public class AmrRenameStrategyMic implements RenameStrategy {

    private static final Pattern AMR_PATTERN = Pattern.compile(
        "\\d{4}-\\d{2}-\\d{2} \\d{2}-\\d{2}-\\d{2} \\(mic\\) (.+?)\\.amr"
    );

    /// Pattern for compact date-time format: mic_20200801-173827.amr
    /// Captures the entire name (including the date part) as title, parses date from it
    private static final Pattern AMR_COMPACT_PATTERN = Pattern.compile(
        "(.+?_(\\d{8})-(\\d{6}))\\.amr"
    );

    @Override
    public Optional<MediaCommands> tryRename(Path value) {

        var fileName = value.getFileName().toString();
        
        // Try compact date-time format first (more specific)
        var matcher = AMR_COMPACT_PATTERN.matcher(fileName);
        if (matcher.matches()) {
            var title = matcher.group(1);
            var dateStr = matcher.group(2) + matcher.group(3);
            var dateTime = parseCompactDateTime(dateStr);
            
            // Create meta from available parts
            var meta = new MediaCommands.AmrMicRecordingMeta(
                dateTime,
                title,
                value);
            
            var cmd = new MediaCommands.RenameMediaItem(value, meta);
            return Optional.of(cmd);
        }
        
        // Try standard format
        matcher = AMR_PATTERN.matcher(fileName);
        
        if (matcher.matches()) {
            // Parse date and time from the filename
            var dateTime = AmrDateTimeParser.parseDateTime(fileName);
            
            // Extract recording title from regex group
            var title = matcher.group(1);

            // Create meta from available parts
            var meta = new MediaCommands.AmrMicRecordingMeta(
                dateTime,
                title,
                value);
            
            var cmd = new MediaCommands.RenameMediaItem(value, meta);
            return Optional.of(cmd);
        }
        
        return Optional.empty();
    }

    /// Parse compact date-time format: yyyyMMddHHmmss
    /// Example: "20200801173827" -> LocalDateTime(2020, 8, 1, 17, 38, 27)
    private static LocalDateTime parseCompactDateTime(String dateTimeString) {
        var formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
        return LocalDateTime.parse(dateTimeString, formatter);
    }

}
