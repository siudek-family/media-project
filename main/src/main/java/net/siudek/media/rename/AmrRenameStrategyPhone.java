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

    /// Pattern for unknown named contact with arrow before (phone): Nieznany kontakt ↙ (phone) 2020-08-05 19-07-44.amr
    private static final Pattern AMR_NAME_ARROW_BEFORE_PHONE_PATTERN = Pattern.compile(
        "(.+?) ([↙↗]) \\(phone\\) (\\d{4}-\\d{2}-\\d{2} \\d{2}-\\d{2}-\\d{2})\\.amr"
    );

    /// Pattern for reversed format with date at end: John Doe (663 444 136) ↗ (phone) 2022-06-18 14-14-47.amr
    /// Also supports international phone: Adrian Cypr (+48 508 459 596) ↗ (phone) 2023-06-08 14-15-23.amr
    private static final Pattern AMR_REVERSED_LOCAL_PATTERN = Pattern.compile(
        "(.+?) \\((\\+?\\d+(?:\\s\\d+)*)\\) ([↙↗]) \\(phone\\) (\\d{4}-\\d{2}-\\d{2} \\d{2}-\\d{2}-\\d{2})\\.amr"
    );

    /// Pattern for reversed format without arrow (direction: UNDEFINED): Helena Dawid (607 739 779) (phone) 2022-08-31 21-16-24.amr
    private static final Pattern AMR_REVERSED_LOCAL_NO_ARROW_PATTERN = Pattern.compile(
        "(.+?) \\((\\d+(?:\\s\\d+)*)\\) \\(phone\\) (\\d{4}-\\d{2}-\\d{2} \\d{2}-\\d{2}-\\d{2})\\.amr"
    );

    /// Pattern for reversed format with phone number only: +48 18 202 00 00 ↗ (phone) 2023-05-27 14-30-22.amr
    /// Also supports extensions: +48 42 638 97 61 ext. 3691829 ↗ (phone) 2023-09-13 18-31-24.amr
    /// Also supports special chars in phone: 717574512,,042629215060_ ↗ (phone) 2023-06-22 16-11-30.amr
    private static final Pattern AMR_REVERSED_PHONE_ONLY_PATTERN = Pattern.compile(
        "([0-9+.,_\\- ]+(?:\\s+ext\\.\\s+\\d+)?) ([↙↗]) \\(phone\\) (\\d{4}-\\d{2}-\\d{2} \\d{2}-\\d{2}-\\d{2})\\.amr"
    );

    /// Pattern for local phone without arrow (defaults to OUTGOING): 2022-10-02 15-01-16 (phone) John Doe (0048695785583).amr
    private static final Pattern AMR_LOCAL_NO_ARROW_PATTERN = Pattern.compile(
        "\\d{4}-\\d{2}-\\d{2} \\d{2}-\\d{2}-\\d{2} \\(phone\\) (.+?) \\((\\d+(?:\\s\\d+)*)\\)\\.amr"
    );

    /// Pattern for unidentified phone number without arrow (direction: UNDEFINED): 44 649 96 84 (phone) 2022-08-16 08-18-00.amr
    private static final Pattern AMR_UNIDENTIFIED_NO_ARROW_PATTERN = Pattern.compile(
        "(\\d+(?:\\s\\d+)*) \\(phone\\) (\\d{4}-\\d{2}-\\d{2} \\d{2}-\\d{2}-\\d{2})\\.amr"
    );

    /// Pattern for Facebook calls (defaults to OUTGOING): 2022-11-08 13-04-02 (facebook) John Doe.amr
    private static final Pattern AMR_FACEBOOK_PATTERN = Pattern.compile(
        "\\d{4}-\\d{2}-\\d{2} \\d{2}-\\d{2}-\\d{2} \\(facebook\\) (.+?)\\.amr"
    );

    /// Pattern for reversed Facebook format with date at end: 0_12 (facebook) 2022-02-18 10-12-13.amr
    private static final Pattern AMR_REVERSED_FACEBOOK_PATTERN = Pattern.compile(
        "(.+?) \\(facebook\\) (\\d{4}-\\d{2}-\\d{2} \\d{2}-\\d{2}-\\d{2})\\.amr"
    );

    /// Pattern for reversed WhatsApp format with date at end: Gerhard Klopper (whatsapp) 2020-09-11 20-04-48.amr
    private static final Pattern AMR_REVERSED_WHATSAPP_PATTERN = Pattern.compile(
        "(.+?) \\(whatsapp\\) (\\d{4}-\\d{2}-\\d{2} \\d{2}-\\d{2}-\\d{2})\\.amr"
    );

    /// Pattern for WhatsApp calls (defaults to OUTGOING): 2020-11-05 21-27-39 (whatsapp) John Doe.amr
    private static final Pattern AMR_WHATSAPP_PATTERN = Pattern.compile(
        "\\d{4}-\\d{2}-\\d{2} \\d{2}-\\d{2}-\\d{2} \\(whatsapp\\) (.+?)\\.amr"
    );

    /// Pattern for reversed Signal format with date at end: ⁩John Doe⁩ (signal) 2022-07-27 11-10-46.amr
    private static final Pattern AMR_REVERSED_SIGNAL_PATTERN = Pattern.compile(
        "(.+?) \\(signal\\) (\\d{4}-\\d{2}-\\d{2} \\d{2}-\\d{2}-\\d{2})\\.amr"
    );

    /// Pattern for Signal calls (defaults to UNDEFINED): 2022-07-27 11-10-46 (signal) John Doe.amr
    private static final Pattern AMR_SIGNAL_PATTERN = Pattern.compile(
        "\\d{4}-\\d{2}-\\d{2} \\d{2}-\\d{2}-\\d{2} \\(signal\\) (.+?)\\.amr"
    );

    /// Pattern for dated recordings with description (undefined direction): 2021-09-17 19-59-50.Some description.amr
    private static final Pattern AMR_DATED_DESCRIPTION_PATTERN = Pattern.compile(
        "\\d{4}-\\d{2}-\\d{2} \\d{2}-\\d{2}-\\d{2}\\.(.+?)\\.amr"
    );

    /// Pattern for compact date format with description (undefined direction): 20200728-184500.Some description.amr
    private static final Pattern AMR_COMPACT_DATE_DESCRIPTION_PATTERN = Pattern.compile(
        "(\\d{8})-(\\d{6})\\.(.+?)\\.amr"
    );

    /// Pattern for compact date format only (undefined direction): 20201014-225441.amr
    private static final Pattern AMR_COMPACT_DATE_ONLY_PATTERN = Pattern.compile(
        "(\\d{8})-(\\d{6})\\.amr"
    );

    /// Pattern for phone prefix format: phone_20200728-111324_0048663444136.amr
    private static final Pattern AMR_PHONE_PREFIX_PATTERN = Pattern.compile(
        "phone_(\\d{8})-(\\d{6})_(\\d+)\\.amr"
    );

    /// Record to hold extracted phone call data from filename patterns
    private record AmrPhoneData(String contactName, String contactPhone, String arrow, LocalDateTime dateTime) {}

    /// name example: 2021-11-14 15-57-45 (phone) John Doe (+48 123 456 789) ↗.amr or (0048123456789) ↙.amr or 2000 ↙.amr
    /// Returns Optional containing MediaCommands if pattern matches, empty Optional otherwise
    @Override
    public Optional<MediaCommands> tryRename(Path value) {
        var fileName = value.getFileName().toString();
        
        // Try patterns in priority order using Optional chaining
        return tryMatchPhonePrefixFormat(fileName)
            .or(() -> tryMatchReversedFormat(fileName))
            .or(() -> tryMatchStandardPhoneWithArrow(fileName))
            .or(() -> tryMatchStandardPhoneNoArrow(fileName))
            .or(() -> tryMatchSocialMediaStandardFormat(fileName))
            .or(() -> tryMatchGenericRecordingStandardFormat(fileName))
            .or(() -> tryMatchCompactDateFormat(fileName))
            .map(data -> {
                var direction = determineDirection(data.arrow, data.contactPhone);
                var meta = new MediaCommands.AmrPhoneCallMeta(
                    data.dateTime,
                    data.contactName,
                    data.contactPhone,
                    direction,
                    value);
                return new MediaCommands.RenameMediaItem(value, meta);
            });
    }

    /// Try phone prefix format: phone_20200728-111324_0048663444136.amr
    /// Pattern: AMR_PHONE_PREFIX_PATTERN
    private Optional<AmrPhoneData> tryMatchPhonePrefixFormat(String fileName) {
        var matcher = AMR_PHONE_PREFIX_PATTERN.matcher(fileName);
        if (matcher.matches()) {
            var dateStr = matcher.group(1) + matcher.group(2);
            var phoneNumber = matcher.group(3);
            return Optional.of(new AmrPhoneData(
                "Nieznany kontakt",
                phoneNumber,
                "↙",  // INCOMING direction for phone_ prefix format
                parseCompactDateTime(dateStr)
            ));
        }
        return Optional.empty();
    }

    /// Try reversed format patterns: contact/phone first, date at end
    /// Patterns: AMR_REVERSED_FACEBOOK_PATTERN, AMR_REVERSED_WHATSAPP_PATTERN, AMR_REVERSED_PHONE_ONLY_PATTERN, AMR_REVERSED_LOCAL_PATTERN
    private Optional<AmrPhoneData> tryMatchReversedFormat(String fileName) {
        // Try reversed Facebook format first (date at end)
        var matcher = AMR_REVERSED_FACEBOOK_PATTERN.matcher(fileName);
        
        if (matcher.matches()) {
            return Optional.of(new AmrPhoneData(
                matcher.group(1),
                "FACEBOOK",
                null,
                AmrDateTimeParser.parseDateTime(matcher.group(2))
            ));
        }
        
        // Try reversed WhatsApp format (date at end)
        matcher = AMR_REVERSED_WHATSAPP_PATTERN.matcher(fileName);
        if (matcher.matches()) {
            return Optional.of(new AmrPhoneData(
                matcher.group(1),
                "WHATSAPP",
                null,
                AmrDateTimeParser.parseDateTime(matcher.group(2))
            ));
        }
        
        // Try reversed Signal format (date at end)
        matcher = AMR_REVERSED_SIGNAL_PATTERN.matcher(fileName);
        if (matcher.matches()) {
            var contactName = matcher.group(1).trim();
            // Remove invisible characters like ⁩ from contact name
            contactName = contactName.replaceAll("[\u2069\u2066]", "");
            return Optional.of(new AmrPhoneData(
                contactName,
                "SIGNAL",
                "UNDEFINED",
                AmrDateTimeParser.parseDateTime(matcher.group(2))
            ));
        }
        
        // Try reversed format with phone only (date at end, no contact name)
        matcher = AMR_REVERSED_PHONE_ONLY_PATTERN.matcher(fileName);
        if (matcher.matches()) {
            var contactPhone = matcher.group(1);
            return Optional.of(new AmrPhoneData(
                contactPhone,
                contactPhone,
                matcher.group(2),
                AmrDateTimeParser.parseDateTime(matcher.group(3))
            ));
        }
        
        // Try reversed format with contact name (date at end)
        matcher = AMR_REVERSED_LOCAL_PATTERN.matcher(fileName);
        if (matcher.matches()) {
            return Optional.of(new AmrPhoneData(
                matcher.group(1),
                matcher.group(2),
                matcher.group(3),
                AmrDateTimeParser.parseDateTime(matcher.group(4))
            ));
        }
        
        // Try reversed format with contact name without arrow (direction: UNDEFINED)
        matcher = AMR_REVERSED_LOCAL_NO_ARROW_PATTERN.matcher(fileName);
        if (matcher.matches()) {
            return Optional.of(new AmrPhoneData(
                matcher.group(1),
                matcher.group(2),
                "UNDEFINED",
                AmrDateTimeParser.parseDateTime(matcher.group(3))
            ));
        }
        
        // Try contact name with arrow before (phone): Nieznany kontakt ↙ (phone) 2020-08-05 19-07-44.amr
        matcher = AMR_NAME_ARROW_BEFORE_PHONE_PATTERN.matcher(fileName);
        if (matcher.matches()) {
            var contactName = matcher.group(1);
            return Optional.of(new AmrPhoneData(
                contactName,
                contactName,
                matcher.group(2),
                AmrDateTimeParser.parseDateTime(matcher.group(3))
            ));
        }
        
        return Optional.empty();
    }

    /// Try standard phone patterns with arrow (date-first format)
    /// Patterns: AMR_INTERNATIONAL_PATTERN, AMR_LOCAL_PATTERN, AMR_UNIDENTIFIED_PATTERN, AMR_NAME_ONLY_PATTERN
    private Optional<AmrPhoneData> tryMatchStandardPhoneWithArrow(String fileName) {
        // Try international pattern (with contact name and + phone)
        var matcher = AMR_INTERNATIONAL_PATTERN.matcher(fileName);
        if (matcher.matches()) {
            return Optional.of(new AmrPhoneData(
                matcher.group(1),
                matcher.group(2),
                matcher.group(3),
                AmrDateTimeParser.parseDateTime(fileName)
            ));
        }
        
        // Try local pattern (with contact name and local phone)
        matcher = AMR_LOCAL_PATTERN.matcher(fileName);
        if (matcher.matches()) {
            return Optional.of(new AmrPhoneData(
                matcher.group(1),
                matcher.group(2),
                matcher.group(3),
                AmrDateTimeParser.parseDateTime(fileName)
            ));
        }
        
        // Try unidentified caller pattern (just ID number)
        matcher = AMR_UNIDENTIFIED_PATTERN.matcher(fileName);
        if (matcher.matches()) {
            var contactId = matcher.group(1);
            return Optional.of(new AmrPhoneData(
                contactId,
                contactId,
                matcher.group(2),
                AmrDateTimeParser.parseDateTime(fileName)
            ));
        }
        
        // Try unknown named contact without phone
        matcher = AMR_NAME_ONLY_PATTERN.matcher(fileName);
        if (matcher.matches()) {
            var contactName = matcher.group(1);
            return Optional.of(new AmrPhoneData(
                contactName,
                contactName,
                matcher.group(2),
                AmrDateTimeParser.parseDateTime(fileName)
            ));
        }
        
        return Optional.empty();
    }

    /// Try standard phone pattern without arrow (defaults to OUTGOING or UNDEFINED)
    /// Patterns: AMR_LOCAL_NO_ARROW_PATTERN, AMR_UNIDENTIFIED_NO_ARROW_PATTERN
    private Optional<AmrPhoneData> tryMatchStandardPhoneNoArrow(String fileName) {
        // Try named contact without arrow (defaults to OUTGOING)
        var matcher = AMR_LOCAL_NO_ARROW_PATTERN.matcher(fileName);
        if (matcher.matches()) {
            return Optional.of(new AmrPhoneData(
                matcher.group(1),
                matcher.group(2),
                null,
                AmrDateTimeParser.parseDateTime(fileName)
            ));
        }
        
        // Try unidentified phone number without arrow (direction: UNDEFINED)
        matcher = AMR_UNIDENTIFIED_NO_ARROW_PATTERN.matcher(fileName);
        if (matcher.matches()) {
            var contactPhone = matcher.group(1);
            return Optional.of(new AmrPhoneData(
                contactPhone,
                contactPhone,
                "UNDEFINED",
                AmrDateTimeParser.parseDateTime(matcher.group(2))
            ));
        }
        
        return Optional.empty();
    }

    /// Try social media patterns (Facebook, WhatsApp) in standard format
    /// Patterns: AMR_FACEBOOK_PATTERN, AMR_WHATSAPP_PATTERN
    private Optional<AmrPhoneData> tryMatchSocialMediaStandardFormat(String fileName) {
        // Try Facebook call pattern (defaults to OUTGOING)
        var matcher = AMR_FACEBOOK_PATTERN.matcher(fileName);
        if (matcher.matches()) {
            return Optional.of(new AmrPhoneData(
                matcher.group(1),
                "FACEBOOK",
                null,
                AmrDateTimeParser.parseDateTime(fileName)
            ));
        }
        
        // Try WhatsApp call pattern (defaults to OUTGOING)
        matcher = AMR_WHATSAPP_PATTERN.matcher(fileName);
        if (matcher.matches()) {
            return Optional.of(new AmrPhoneData(
                matcher.group(1),
                "WHATSAPP",
                null,
                AmrDateTimeParser.parseDateTime(fileName)
            ));
        }
        
        // Try Signal call pattern (defaults to UNDEFINED)
        matcher = AMR_SIGNAL_PATTERN.matcher(fileName);
        if (matcher.matches()) {
            var contactName = matcher.group(1).trim();
            // Remove invisible characters like ⁩ from contact name
            contactName = contactName.replaceAll("[\u2069\u2066]", "");
            return Optional.of(new AmrPhoneData(
                contactName,
                "SIGNAL",
                "UNDEFINED",
                AmrDateTimeParser.parseDateTime(fileName)
            ));
        }
        
        return Optional.empty();
    }

    /// Try generic recording pattern with standard datetime format
    /// Pattern: AMR_DATED_DESCRIPTION_PATTERN
    private Optional<AmrPhoneData> tryMatchGenericRecordingStandardFormat(String fileName) {
        var matcher = AMR_DATED_DESCRIPTION_PATTERN.matcher(fileName);
        if (matcher.matches()) {
            return Optional.of(new AmrPhoneData(
                matcher.group(1),
                "UNKNOWN",
                null,
                AmrDateTimeParser.parseDateTime(fileName)
            ));
        }
        return Optional.empty();
    }

    /// Try compact date format patterns (yyyyMMddHHmmss)
    /// Patterns: AMR_COMPACT_DATE_DESCRIPTION_PATTERN, AMR_COMPACT_DATE_ONLY_PATTERN
    private Optional<AmrPhoneData> tryMatchCompactDateFormat(String fileName) {
        // Try compact date format with description (undefined direction)
        var matcher = AMR_COMPACT_DATE_DESCRIPTION_PATTERN.matcher(fileName);
        if (matcher.matches()) {
            var dateStr = matcher.group(1) + matcher.group(2);
            return Optional.of(new AmrPhoneData(
                matcher.group(3),
                "UNKNOWN",
                null,
                parseCompactDateTime(dateStr)
            ));
        }
        
        // Try compact date format only (undefined direction)
        matcher = AMR_COMPACT_DATE_ONLY_PATTERN.matcher(fileName);
        if (matcher.matches()) {
            var dateStr = matcher.group(1) + matcher.group(2);
            return Optional.of(new AmrPhoneData(
                "UNKNOWN",
                "UNKNOWN",
                null,
                parseCompactDateTime(dateStr)
            ));
        }
        
        return Optional.empty();
    }

    /// Determine call direction based on arrow and contactPhone
    /// "UNDEFINED" arrow explicitly sets direction to UNDEFINED
    /// null arrow defaults to OUTGOING unless contactPhone is UNKNOWN (then UNDEFINED)
    private MediaCommands.CallDirection determineDirection(String arrow, String contactPhone) {
        if ("UNDEFINED".equals(arrow)) {
            return MediaCommands.CallDirection.UNDEFINED;
        }
        if (arrow == null) {
            return contactPhone.equals("UNKNOWN") 
                ? MediaCommands.CallDirection.UNDEFINED 
                : MediaCommands.CallDirection.OUTGOING;
        }
        return switch(arrow) {
            case "↙" -> MediaCommands.CallDirection.INCOMING;
            case "↗" -> MediaCommands.CallDirection.OUTGOING;
            default -> throw new IllegalStateException("Unexpected arrow value: " + arrow);
        };
    }

    /// Parse compact date-time format: yyyyMMddHHmmss
    /// Example: "20200728184500" -> LocalDateTime(2020, 7, 28, 18, 45, 0)
    private static LocalDateTime parseCompactDateTime(String dateTimeString) {
        var formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
        return LocalDateTime.parse(dateTimeString, formatter);
    }

}
