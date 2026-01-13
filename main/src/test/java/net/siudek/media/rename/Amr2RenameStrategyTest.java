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

@DisplayName("Amr2RenameStrategy")
class Amr2RenameStrategyTest {

    private final CommandsListener commandsListener = mock();
    private final Amr2RenameStrategy strategy = new Amr2RenameStrategy(commandsListener);

    @Test
    @DisplayName("should rename microphone recording AMR file with date-time (mic) pattern")
    void shouldRenameMicrophoneRecordingAMRFile(@TempDir Path tempDir) {
        var filePath = tempDir.resolve("2021-11-14 17-49-05 (mic) Nagrywanie dyktafonu.amr");
        
        var result = strategy.tryRename(filePath);
        
        assertThat(result).isTrue();
        var meta = new MediaCommands.AmrMicRecordingMeta(
            LocalDateTime.of(2021, 11, 14, 17, 49, 5),
            "Nagrywanie dyktafonu",
            filePath
        );
        verify(commandsListener).on(new MediaCommands.RenameMediaItem(filePath, meta));
    }

    @Test
    @DisplayName("should return false for non-matching file pattern")
    void shouldReturnFalseForNonMatchingPattern() {
        var filePath = Path.of("2021-11-14_17-49-05_some_file.amr");
        
        var result = strategy.tryRename(filePath);
        
        assertThat(result).isFalse();
    }

}
