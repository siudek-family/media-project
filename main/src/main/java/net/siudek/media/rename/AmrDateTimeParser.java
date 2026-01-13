package net.siudek.media.rename;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/// Utility class for parsing AMR filename date-time patterns.
/// Extracts and parses date-time from AMR filenames with format: yyyy-MM-dd HH-mm-ss
public final class AmrDateTimeParser {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH-mm-ss");

    private AmrDateTimeParser() {
        // utility class
    }

    /// Parses date-time from AMR filename format.
    /// Expected format: yyyy-MM-dd HH-mm-ss (first 19 characters of filename)
    /// Example: "2021-11-14 15-57-45" -> LocalDateTime(2021, 11, 14, 15, 57, 45)
    ///
    /// @param fileName the filename containing date-time prefix
    /// @return parsed LocalDateTime
    /// @throws DateTimeParseException if the date-time cannot be parsed
    /// @throws StringIndexOutOfBoundsException if fileName is shorter than 19 characters
    public static LocalDateTime parseDateTime(String fileName) {
        var dateTimeString = fileName.substring(0, 19);
        return LocalDateTime.parse(dateTimeString, FORMATTER);
    }

}
