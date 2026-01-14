package net.siudek.media.rename;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Path;
import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import net.siudek.media.MediaCommands;
import net.siudek.media.MediaCommands.CallDirection;

@DisplayName("AmrRenameStrategyPhone")
class AmrRenameStrategyPhoneTest {

    private final AmrRenameStrategyPhone strategy = new AmrRenameStrategyPhone();

    @ParameterizedTest
    @CsvSource(delimiter = '|', textBlock = """
        2021-11-14 15-57-45 (phone) Jan Kowalski (+48 503 594 583) ↗.amr | Jan Kowalski | +48 503 594 583 | OUTGOING | 2021-11-14T15:57:45
        2021-11-14 15-57-45 (phone) Jan Kowalski (0048604066737) ↙.amr | Jan Kowalski | 0048604066737 | INCOMING | 2021-11-14T15:57:45
        2021-11-15 18-12-28 (phone) Janek (667 044 821) ↙.amr | Janek | 667 044 821 | INCOMING | 2021-11-15T18:12:28
        2021-11-19 14-44-06 (phone) 42 237 22 28 ↙.amr | 42 237 22 28 | 42 237 22 28 | INCOMING | 2021-11-19T14:44:06
        2021-11-19 18-02-07 (phone) Nieznany kontakt ↙.amr | Nieznany kontakt | Nieznany kontakt | INCOMING | 2021-11-19T18:02:07
        2021-11-14 19-49-35 (phone) 2000 ↙.amr | 2000 | 2000 | INCOMING | 2021-11-14T19:49:35
        John Doe (663 444 136) ↗ (phone) 2022-06-18 14-14-47.amr | John Doe | 663 444 136 | OUTGOING | 2022-06-18T14:14:47
        2022-10-02 15-01-16 (phone) John Doe (0048695785583).amr | John Doe | 0048695785583 | OUTGOING | 2022-10-02T15:01:16
        2022-11-08 13-04-02 (facebook) John Doe.amr | John Doe | FACEBOOK | OUTGOING | 2022-11-08T13:04:02
        +48 18 202 00 00 ↗ (phone) 2023-05-27 14-30-22.amr | +48 18 202 00 00 | +48 18 202 00 00 | OUTGOING | 2023-05-27T14:30:22
        +48 42 638 97 61 ext. 3691829 ↗ (phone) 2023-09-13 18-31-24.amr | +48 42 638 97 61 ext. 3691829 | +48 42 638 97 61 ext. 3691829 | OUTGOING | 2023-09-13T18:31:24
        0_12 (facebook) 2022-02-18 10-12-13.amr | 0_12 | FACEBOOK | OUTGOING | 2022-02-18T10:12:13
        2020-11-05 21-27-39 (whatsapp) John Doe.amr | John Doe | WHATSAPP | OUTGOING | 2020-11-05T21:27:39
        20200728-184500.Some description.amr | Some description | UNKNOWN | UNDEFINED | 2020-07-28T18:45:00
        2021-09-17 19-59-50.Asia - kanapki na wyjazd.amr | Asia - kanapki na wyjazd | UNKNOWN | UNDEFINED | 2021-09-17T19:59:50
        20201014-225441.amr | UNKNOWN | UNKNOWN | UNDEFINED | 2020-10-14T22:54:41
        44 649 96 84 (phone) 2022-08-16 08-18-00.amr | 44 649 96 84 | 44 649 96 84 | UNDEFINED | 2022-08-16T08:18:00
        717574512,,042629215060_ ↗ (phone) 2023-06-22 16-11-30.amr | 717574512,,042629215060_ | 717574512,,042629215060_ | OUTGOING | 2023-06-22T16:11:30
        Adrian Cypr (+48 508 459 596) ↗ (phone) 2023-06-08 14-15-23.amr | Adrian Cypr | +48 508 459 596 | OUTGOING | 2023-06-08T14:15:23
        Gerhard Klopper (whatsapp) 2020-09-11 20-04-48.amr | Gerhard Klopper | WHATSAPP | OUTGOING | 2020-09-11T20:04:48
        """)
    @DisplayName("should rename phone call AMR file with valid patterns")
    void shouldRenamePhoneCallAMRFile(String fileName, String expectedContactName, String expectedPhone, CallDirection expectedDirection, LocalDateTime expectedDateTime, @TempDir Path tempDir) {
        var filePath = tempDir.resolve(fileName);
        
        var result = strategy.tryRename(filePath);
        
        assertThat(result).isPresent();
        var command = (MediaCommands.RenameMediaItem) result.get();
        assertThat(command.from()).isEqualTo(filePath);
        
        var meta = (MediaCommands.AmrPhoneCallMeta) command.meta();
        assertThat(meta.dateTime()).isEqualTo(expectedDateTime);
        assertThat(meta.contactName()).isEqualTo(expectedContactName);
        assertThat(meta.contactPhone()).isEqualTo(expectedPhone);
        assertThat(meta.direction()).isEqualTo(expectedDirection);
        assertThat(meta.location()).isEqualTo(filePath);
    }

    @Test
    @DisplayName("should return empty Optional for non-matching file pattern")
    void shouldReturnEmptyForNonMatchingPattern() {
        var filePath = Path.of("2021-11-14_15-57-45_some_file.amr");
        
        var result = strategy.tryRename(filePath);
        
        assertThat(result).isEmpty();
    }

}
