package net.siudek.media.rename;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.nio.file.Path;
import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.api.DisplayName;

import net.siudek.media.CommandsListener;
import net.siudek.media.MediaCommands;

@DisplayName("Amr1RenameStrategy")
class Amr1RenameStrategyTest {

    private final CommandsListener commandsListener = mock();
    private final Amr1RenameStrategy strategy = new Amr1RenameStrategy(commandsListener);

    @Test
    @DisplayName("should rename phone call AMR file with date-time space-separated pattern")
    void shouldRenamePhoneCallAMRFile(@TempDir Path tempDir) {
        var filePath = tempDir.resolve("2021-11-14 15-57-45 (phone) Jan Kowalski (+48 503 594 583) ↗.amr");
        
        var result = strategy.tryRename(filePath);
        
        assertThat(result).isTrue();
        var meta = new MediaCommands.AmrPhoneCallMeta(
            LocalDateTime.of(2021, 11, 14, 15, 57, 45),
            "Jan Kowalski",
            "+48 503 594 583",
            "outcoming",
            filePath
        );
        verify(commandsListener).on(new MediaCommands.RenameMediaItem(filePath, meta));
    }

    @Test
    @DisplayName("should rename phone call AMR file with compact phone number format (00xx...)")
    void shouldRenamePhoneCallAMRFileWithCompactPhoneNumber(@TempDir Path tempDir) {
        var filePath = tempDir.resolve("2021-11-14 15-57-45 (phone) Jan Kowalski (0048604066737) ↘.amr");
        
        var result = strategy.tryRename(filePath);
        
        assertThat(result).isTrue();
        var meta = new MediaCommands.AmrPhoneCallMeta(
            LocalDateTime.of(2021, 11, 14, 15, 57, 45),
            "Jan Kowalski",
            "0048604066737",
            "incoming",
            filePath
        );
        verify(commandsListener).on(new MediaCommands.RenameMediaItem(filePath, meta));
    }

    @Test
    @DisplayName("should return false for non-matching file pattern")
    void shouldReturnFalseForNonMatchingPattern() {
        var filePath = Path.of("/path/2021-11-14_15-57-45_some_file.amr");
        
        var result = strategy.tryRename(filePath);
        
        assertThat(result).isFalse();
    }

}
