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
        2021-11-14 15-57-45 (phone) Jan Kowalski (+48 503 594 583) ↗.amr | Jan Kowalski | +48 503 594 583 | OUTGOING
        2021-11-14 15-57-45 (phone) Jan Kowalski (0048604066737) ↙.amr | Jan Kowalski | 0048604066737 | INCOMING
        2021-11-15 18-12-28 (phone) Janek (667 044 821) ↙.amr | Janek | 667 044 821 | INCOMING
        2021-11-19 14-44-06 (phone) 42 237 22 28 ↙.amr | 42 237 22 28 | 42 237 22 28 | INCOMING
        2021-11-19 18-02-07 (phone) Nieznany kontakt ↙.amr | Nieznany kontakt | Nieznany kontakt | INCOMING
        2021-11-14 19-49-35 (phone) 2000 ↙.amr | 2000 | 2000 | INCOMING
        John Doe (663 444 136) ↗ (phone) 2022-06-18 14-14-47.amr | John Doe | 663 444 136 | OUTGOING
        2022-10-02 15-01-16 (phone) John Doe (0048695785583).amr | John Doe | 0048695785583 | OUTGOING
        2022-11-08 13-04-02 (facebook) John Doe.amr | John Doe | FACEBOOK | OUTGOING
        +48 18 202 00 00 ↗ (phone) 2023-05-27 14-30-22.amr | +48 18 202 00 00 | +48 18 202 00 00 | OUTGOING
        """)
    @DisplayName("should rename phone call AMR file with valid patterns")
    void shouldRenamePhoneCallAMRFile(String fileName, String expectedContactName, String expectedPhone, CallDirection expectedDirection, @TempDir Path tempDir) {
        var filePath = tempDir.resolve(fileName);
        
        var result = strategy.tryRename(filePath);
        
        assertThat(result).isPresent();
        var command = (MediaCommands.RenameMediaItem) result.get();
        assertThat(command.from()).isEqualTo(filePath);
        
        var meta = (MediaCommands.AmrPhoneCallMeta) command.meta();
        assertThat(meta.contactName()).isEqualTo(expectedContactName);
        assertThat(meta.contactPhone()).isEqualTo(expectedPhone);
        assertThat(meta.direction()).isEqualTo(expectedDirection);
        assertThat(meta.location()).isEqualTo(filePath);
    }

    @Test
    @DisplayName("should return empty Optional for non-matching file pattern")
    void shouldReturnEmptyForNonMatchingPattern() {
        var filePath = Path.of("/path/2021-11-14_15-57-45_some_file.amr");
        
        var result = strategy.tryRename(filePath);
        
        assertThat(result).isEmpty();
    }

}
