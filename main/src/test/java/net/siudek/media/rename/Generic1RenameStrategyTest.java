package net.siudek.media.rename;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Path;
import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.api.DisplayName;

import net.siudek.media.MediaCommands;

@DisplayName("Generic1RenameStrategy")
class Generic1RenameStrategyTest {

    private final Generic1RenameStrategy strategy = new Generic1RenameStrategy();

    @Test
    @DisplayName("should rename file with yyyyMMdd_hhmmss pattern to yyyyMMdd-hhmmss pattern")
    void shouldRenameFileWithUnderscoreSeparator(@TempDir Path tempDir) {
        var filePath = tempDir.resolve("20231225_153045.jpg");
        
        var result = strategy.tryRename(filePath);
        
        assertThat(result).isPresent();
        var command = (MediaCommands.RenameMediaItem) result.get();
        assertThat(command.from()).isEqualTo(filePath);
        
        var meta = (MediaCommands.GenericMeta) command.meta();
        assertThat(meta.date()).isEqualTo(LocalDateTime.of(2023, 12, 25, 15, 30, 45));
        assertThat(meta.extension()).isEqualTo("jpg");
        assertThat(meta.location()).isEqualTo(filePath);
    }

    @Test
    @DisplayName("should return empty Optional for non-matching file pattern")
    void shouldReturnEmptyForNonMatchingPattern(@TempDir Path tempDir) {
        var filePath = tempDir.resolve("20231225-153045.jpg");
        
        var result = strategy.tryRename(filePath);
        
        assertThat(result).isEmpty();
    }

}
