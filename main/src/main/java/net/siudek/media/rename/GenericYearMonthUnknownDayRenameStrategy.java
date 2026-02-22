package net.siudek.media.rename;

import java.nio.file.Path;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Optional;

import org.springframework.stereotype.Component;

import net.siudek.media.MediaCommands;

@Component
public class GenericYearMonthUnknownDayRenameStrategy implements RenameStrategy {

    private static final DateTimeFormatter YEAR_MONTH_FORMATTER = DateTimeFormatter.ofPattern("yyyyMM");

    /// normalize yyyyMM__.* to yyyyMM__.*
    @Override
    public Optional<MediaCommands> tryRename(Path value) {
        var fileName = value.getFileName().toString();

        if (!fileName.matches("\\d{6}__\\..+")) {
            return Optional.empty();
        }

        var yearMonthPart = fileName.substring(0, 6);

        try {
            var date = YearMonth.parse(yearMonthPart, YEAR_MONTH_FORMATTER);
            var extension = fileName.substring(9);
            var meta = new MediaCommands.GenericMetaYM(date, extension, value);
            var cmd = new MediaCommands.RenameMediaItem(value, meta);
            return Optional.of(cmd);
        } catch (DateTimeParseException e) {
            return Optional.empty();
        }
    }
}