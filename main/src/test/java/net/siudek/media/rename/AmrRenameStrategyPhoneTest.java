package net.siudek.media.rename;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Path;
import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.api.DisplayName;

import net.siudek.media.MediaCommands;

@DisplayName("AmrRenameStrategyPhone")
class AmrRenameStrategyPhoneTest {

    private final AmrRenameStrategyPhone strategy = new AmrRenameStrategyPhone();

    @Test
    @DisplayName("should rename phone call AMR file with date-time space-separated pattern")
    void shouldRenamePhoneCallAMRFile(@TempDir Path tempDir) {
        var filePath = tempDir.resolve("2021-11-14 15-57-45 (phone) Jan Kowalski (+48 503 594 583) ↗.amr");
        
        var result = strategy.tryRename(filePath);
        
        assertThat(result).isPresent();
        var command = (MediaCommands.RenameMediaItem) result.get();
        assertThat(command.from()).isEqualTo(filePath);
        
        var meta = (MediaCommands.AmrPhoneCallMeta) command.meta();
        assertThat(meta.dateTime()).isEqualTo(LocalDateTime.of(2021, 11, 14, 15, 57, 45));
        assertThat(meta.contactName()).isEqualTo("Jan Kowalski");
        assertThat(meta.contactPhone()).isEqualTo("+48 503 594 583");
        assertThat(meta.direction()).isEqualTo("outcoming");
        assertThat(meta.location()).isEqualTo(filePath);
    }

    @Test
    @DisplayName("should rename phone call AMR file with compact phone number format (00xx...)")
    void shouldRenamePhoneCallAMRFileWithCompactPhoneNumber(@TempDir Path tempDir) {
        var filePath = tempDir.resolve("2021-11-14 15-57-45 (phone) Jan Kowalski (0048604066737) ↘.amr");
        
        var result = strategy.tryRename(filePath);
        
        assertThat(result).isPresent();
        var command = (MediaCommands.RenameMediaItem) result.get();
        assertThat(command.from()).isEqualTo(filePath);
        
        var meta = (MediaCommands.AmrPhoneCallMeta) command.meta();
        assertThat(meta.dateTime()).isEqualTo(LocalDateTime.of(2021, 11, 14, 15, 57, 45));
        assertThat(meta.contactName()).isEqualTo("Jan Kowalski");
        assertThat(meta.contactPhone()).isEqualTo("0048604066737");
        assertThat(meta.direction()).isEqualTo("incoming");
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
