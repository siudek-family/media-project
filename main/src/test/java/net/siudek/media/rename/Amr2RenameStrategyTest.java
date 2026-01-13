package net.siudek.media.rename;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Path;
import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.api.DisplayName;

import net.siudek.media.MediaCommands;

@DisplayName("Amr2RenameStrategy")
class Amr2RenameStrategyTest {

    private final Amr2RenameStrategy strategy = new Amr2RenameStrategy();

    @Test
    @DisplayName("should rename microphone recording AMR file with date-time (mic) pattern")
    void shouldRenameMicrophoneRecordingAMRFile(@TempDir Path tempDir) {
        var filePath = tempDir.resolve("2021-11-14 17-49-05 (mic) Nagrywanie dyktafonu.amr");
        
        var result = strategy.tryRename(filePath);
        
        assertThat(result).isPresent();
        var command = (MediaCommands.RenameMediaItem) result.get();
        assertThat(command.from()).isEqualTo(filePath);
        
        var meta = (MediaCommands.AmrMicRecordingMeta) command.meta();
        assertThat(meta.dateTime()).isEqualTo(LocalDateTime.of(2021, 11, 14, 17, 49, 5));
        assertThat(meta.title()).isEqualTo("Nagrywanie dyktafonu");
        assertThat(meta.location()).isEqualTo(filePath);
    }

    @Test
    @DisplayName("should return empty Optional for non-matching file pattern")
    void shouldReturnEmptyForNonMatchingPattern() {
        var filePath = Path.of("2021-11-14_17-49-05_some_file.amr");
        
        var result = strategy.tryRename(filePath);
        
        assertThat(result).isEmpty();
    }

}
