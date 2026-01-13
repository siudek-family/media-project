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

@DisplayName("AmrRenameStrategyPhone")
class AmrRenameStrategyPhoneTest {

    private final AmrRenameStrategyPhone strategy = new AmrRenameStrategyPhone();

    @ParameterizedTest
    @CsvSource(delimiter = '|', textBlock = """
        2021-11-14 15-57-45 (phone) Jan Kowalski (+48 503 594 583) ↗.amr | Jan Kowalski | +48 503 594 583 | outcoming
        2021-11-14 15-57-45 (phone) Jan Kowalski (0048604066737) ↙.amr | Jan Kowalski | 0048604066737 | incoming
        """)
    @DisplayName("should rename phone call AMR file with valid patterns")
    void shouldRenamePhoneCallAMRFile(String fileName, String expectedContactName, String expectedPhone, String expectedDirection, @TempDir Path tempDir) {
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
