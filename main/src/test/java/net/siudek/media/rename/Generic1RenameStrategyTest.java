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

@DisplayName("Generic1RenameStrategy")
class Generic1RenameStrategyTest {

    private final CommandsListener commandsListener = mock();
    private final Generic1RenameStrategy strategy = new Generic1RenameStrategy(commandsListener);

    @Test
    @DisplayName("should rename file with yyyyMMdd_hhmmss pattern to yyyyMMdd-hhmmss pattern")
    void shouldRenameFileWithUnderscoreSeparator(@TempDir Path tempDir) {
        var filePath = tempDir.resolve("20231225_153045.jpg");
        
        var result = strategy.tryRename(filePath);
        
        assertThat(result).isTrue();
        verify(commandsListener).on(new MediaCommands.RenameMediaItem(filePath, new MediaCommands.GenericMeta(
            LocalDateTime.of(2023, 12, 25, 15, 30, 45),
            "jpg",
            filePath
        )));
    }

    @Test
    @DisplayName("should return false for non-matching file pattern")
    void shouldReturnFalseForNonMatchingPattern(@TempDir Path tempDir) {
        var filePath = tempDir.resolve("20231225-153045.jpg");
        
        var result = strategy.tryRename(filePath);
        
        assertThat(result).isFalse();
    }

}
