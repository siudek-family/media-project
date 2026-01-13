package net.siudek.media.rename;

import java.nio.file.Path;
import java.util.regex.Pattern;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import net.siudek.media.CommandsListener;
import net.siudek.media.MediaCommands;

@RequiredArgsConstructor
@Component
/// Example to match: 2021-11-14 17-49-05 (mic) Nagrywanie dyktafonu.amr
public class Amr2RenameStrategy implements RenameStrategy {

    private final CommandsListener commandsListener;

    private static final Pattern AMR_PATTERN = Pattern.compile(
        "\\d{4}-\\d{2}-\\d{2} \\d{2}-\\d{2}-\\d{2} \\(mic\\) (.+?)\\.amr"
    );

    @Override
    public boolean tryRename(Path value) {

        var fileName = value.getFileName().toString();
        var matcher = AMR_PATTERN.matcher(fileName);
        
        if (!matcher.matches()) {
            return false;
        }
        
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
        commandsListener.on(cmd);
        return true;
    }

}
