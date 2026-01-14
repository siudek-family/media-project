package net.siudek.media.rename;

import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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

    /// Pattern for reversed format with date at end: John Doe (663 444 136) ↗ (phone) 2022-06-18 14-14-47.amr
    private static final Pattern AMR_REVERSED_LOCAL_PATTERN = Pattern.compile(
        "(.+?) \\((\\d+(?:\\s\\d+)*)\\) ([↙↗]) \\(phone\\) (\\d{4}-\\d{2}-\\d{2} \\d{2}-\\d{2}-\\d{2})\\.amr"
    );

    /// Pattern for reversed format with phone number only: +48 18 202 00 00 ↗ (phone) 2023-05-27 14-30-22.amr
    /// Also supports extensions: +48 42 638 97 61 ext. 3691829 ↗ (phone) 2023-09-13 18-31-24.amr
    private static final Pattern AMR_REVERSED_PHONE_ONLY_PATTERN = Pattern.compile(
        "(\\+?\\d+(?:\\s\\d+)*(?:\\sext\\.\\s\\d+)?) ([↙↗]) \\(phone\\) (\\d{4}-\\d{2}-\\d{2} \\d{2}-\\d{2}-\\d{2})\\.amr"
    );

    /// Pattern for local phone without arrow (defaults to OUTGOING): 2022-10-02 15-01-16 (phone) John Doe (0048695785583).amr
    private static final Pattern AMR_LOCAL_NO_ARROW_PATTERN = Pattern.compile(
        "\\d{4}-\\d{2}-\\d{2} \\d{2}-\\d{2}-\\d{2} \\(phone\\) (.+?) \\((\\d+(?:\\s\\d+)*)\\)\\.amr"
    );

    /// Pattern for Facebook calls (defaults to OUTGOING): 2022-11-08 13-04-02 (facebook) John Doe.amr
    private static final Pattern AMR_FACEBOOK_PATTERN = Pattern.compile(
        "\\d{4}-\\d{2}-\\d{2} \\d{2}-\\d{2}-\\d{2} \\(facebook\\) (.+?)\\.amr"
    );

    /// Pattern for reversed Facebook format with date at end: 0_12 (facebook) 2022-02-18 10-12-13.amr
    private static final Pattern AMR_REVERSED_FACEBOOK_PATTERN = Pattern.compile(
        "(.+?) \\(facebook\\) (\\d{4}-\\d{2}-\\d{2} \\d{2}-\\d{2}-\\d{2})\\.amr"
    );

    /// Pattern for WhatsApp calls (defaults to OUTGOING): 2020-11-05 21-27-39 (whatsapp) John Doe.amr
    private static final Pattern AMR_WHATSAPP_PATTERN = Pattern.compile(
        "\\d{4}-\\d{2}-\\d{2} \\d{2}-\\d{2}-\\d{2} \\(whatsapp\\) (.+?)\\.amr"
    );

    /// Pattern for compact date format with description (undefined direction): 20200728-184500.Some description.amr
    private static final Pattern AMR_COMPACT_DATE_DESCRIPTION_PATTERN = Pattern.compile(
        "(\\d{8})-(\\d{6})\\.(.+?)\\.amr"
    );

    /// Pattern for compact date format only (undefined direction): 20201014-225441.amr
    private static final Pattern AMR_COMPACT_DATE_ONLY_PATTERN = Pattern.compile(
        "(\\d{8})-(\\d{6})\\.amr"
    );

    /// name example: 2021-11-14 15-57-45 (phone) John Doe (+48 123 456 789) ↗.amr or (0048123456789) ↙.amr or 2000 ↙.amr
    /// Returns Optional containing MediaCommands if pattern matches, empty Optional otherwise
    @Override
    public Optional<MediaCommands> tryRename(Path value) {

        var fileName = value.getFileName().toString();
        
        // Try reversed Facebook format first (date at end)
        var matcher = AMR_REVERSED_FACEBOOK_PATTERN.matcher(fileName);
        String contactName;
        String contactPhone;
        String arrow;
        LocalDateTime dateTime;
        
        if (matcher.matches()) {
            contactName = matcher.group(1);
            contactPhone = "FACEBOOK";  // Set phone to FACEBOOK to indicate platform
            arrow = null;  // No arrow means OUTGOING by default
            dateTime = AmrDateTimeParser.parseDateTime(matcher.group(2));
        } else {
            // Try reversed format with phone only (date at end, no contact name)
            matcher = AMR_REVERSED_PHONE_ONLY_PATTERN.matcher(fileName);
        
            if (matcher.matches()) {
                contactPhone = matcher.group(1);
                contactName = contactPhone;  // Use phone as contact name when no name provided
                arrow = matcher.group(2);
                dateTime = AmrDateTimeParser.parseDateTime(matcher.group(3));
            } else {
                // Try reversed format with contact name (date at end)
                matcher = AMR_REVERSED_LOCAL_PATTERN.matcher(fileName);
        
                if (matcher.matches()) {
                    contactName = matcher.group(1);
                    contactPhone = matcher.group(2);
                    arrow = matcher.group(3);
                    dateTime = AmrDateTimeParser.parseDateTime(matcher.group(4));
                } else {
                // Try international pattern (with contact name and + phone)
                matcher = AMR_INTERNATIONAL_PATTERN.matcher(fileName);
        
                if (matcher.matches()) {
                    contactName = matcher.group(1);
                    contactPhone = matcher.group(2);
                    arrow = matcher.group(3);
                    dateTime = AmrDateTimeParser.parseDateTime(fileName);
                } else {
                    // Try local pattern (with contact name and local phone)
                    matcher = AMR_LOCAL_PATTERN.matcher(fileName);
                if (matcher.matches()) {
                    contactName = matcher.group(1);
                    contactPhone = matcher.group(2);
                    arrow = matcher.group(3);
                    dateTime = AmrDateTimeParser.parseDateTime(fileName);
                } else {
                    // Try local pattern without arrow (defaults to OUTGOING)
                    matcher = AMR_LOCAL_NO_ARROW_PATTERN.matcher(fileName);
                    if (matcher.matches()) {
                        contactName = matcher.group(1);
                        contactPhone = matcher.group(2);
                        arrow = null;  // No arrow means OUTGOING by default
                        dateTime = AmrDateTimeParser.parseDateTime(fileName);
                    } else {
                        // Try Facebook call pattern (defaults to OUTGOING)
                        matcher = AMR_FACEBOOK_PATTERN.matcher(fileName);
                        if (matcher.matches()) {
                            contactName = matcher.group(1);
                            contactPhone = "FACEBOOK";  // Set phone to FACEBOOK to indicate platform
                            arrow = null;  // No arrow means OUTGOING by default
                            dateTime = AmrDateTimeParser.parseDateTime(fileName);
                        } else {
                            // Try WhatsApp call pattern (defaults to OUTGOING)
                            matcher = AMR_WHATSAPP_PATTERN.matcher(fileName);
                            if (matcher.matches()) {
                                contactName = matcher.group(1);
                                contactPhone = "WHATSAPP";  // Set phone to WHATSAPP to indicate platform
                                arrow = null;  // No arrow means OUTGOING by default
                                dateTime = AmrDateTimeParser.parseDateTime(fileName);
                            } else {
                                // Try compact date format with description (undefined direction)
                                matcher = AMR_COMPACT_DATE_DESCRIPTION_PATTERN.matcher(fileName);
                                if (matcher.matches()) {
                                    var dateStr = matcher.group(1) + matcher.group(2);  // Combine date and time
                                    dateTime = parseCompactDateTime(dateStr);
                                    contactName = matcher.group(3);
                                    contactPhone = "UNKNOWN";
                                    arrow = null;  // No arrow, will map to UNDEFINED
                                } else {
                                    // Try compact date format only (undefined direction)
                                    matcher = AMR_COMPACT_DATE_ONLY_PATTERN.matcher(fileName);
                                    if (matcher.matches()) {
                                        var dateStr = matcher.group(1) + matcher.group(2);  // Combine date and time
                                        dateTime = parseCompactDateTime(dateStr);
                                        contactName = "UNKNOWN";
                                        contactPhone = "UNKNOWN";
                                        arrow = null;  // No arrow, will map to UNDEFINED
                                    } else {
                                        // Try unknown named contact without phone
                                        matcher = AMR_NAME_ONLY_PATTERN.matcher(fileName);
                                        if (matcher.matches()) {
                                            contactName = matcher.group(1);
                                            contactPhone = matcher.group(1);
                                            arrow = matcher.group(2);
                                            dateTime = AmrDateTimeParser.parseDateTime(fileName);
                                        } else {
                                            // Try unidentified caller pattern (just ID number)
                                            matcher = AMR_UNIDENTIFIED_PATTERN.matcher(fileName);
                                            if (!matcher.matches()) {
                                                return Optional.empty();
                                            }
                                            contactName = matcher.group(1);
                                            contactPhone = matcher.group(1);  // Use ID as phone for unidentified
                                            arrow = matcher.group(2);
                                            dateTime = AmrDateTimeParser.parseDateTime(fileName);
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                }
            }
            }
        }
        
        // Determine call direction based on arrow (null means OUTGOING by default, unless it's a compact date format)
        var direction = (arrow == null && contactPhone.equals("UNKNOWN")) ? MediaCommands.CallDirection.UNDEFINED :
                        arrow == null ? MediaCommands.CallDirection.OUTGOING : switch(arrow) {
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

    /// Parse compact date-time format: yyyyMMddHHmmss
    /// Example: "20200728184500" -> LocalDateTime(2020, 7, 28, 18, 45, 0)
    private static LocalDateTime parseCompactDateTime(String dateTimeString) {
        var formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
        return LocalDateTime.parse(dateTimeString, formatter);
    }

}
