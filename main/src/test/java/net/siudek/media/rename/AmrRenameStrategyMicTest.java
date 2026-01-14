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

@DisplayName("AmrRenameStrategyMic")
class AmrRenameStrategyMicTest {

    private final AmrRenameStrategyMic strategy = new AmrRenameStrategyMic();

    @ParameterizedTest
    @CsvSource(delimiter = '|', textBlock = """
        2021-11-14 17-49-05 (mic) Nagrywanie dyktafonu.amr | Nagrywanie dyktafonu | 2021-11-14T17:49:05
        mic_20200801-173827.amr | mic_20200801-173827 | 2020-08-01T17:38:27
        """)
    @DisplayName("should rename microphone recording AMR file with valid patterns")
    void shouldRenameMicrophoneRecordingAMRFile(String fileName, String expectedTitle, LocalDateTime expectedDateTime, @TempDir Path tempDir) {
        var filePath = tempDir.resolve(fileName);
        
        var result = strategy.tryRename(filePath);
        
        assertThat(result).isPresent();
        var command = (MediaCommands.RenameMediaItem) result.get();
        assertThat(command.from()).isEqualTo(filePath);
        
        var meta = (MediaCommands.AmrMicRecordingMeta) command.meta();
        assertThat(meta.dateTime()).isEqualTo(expectedDateTime);
        assertThat(meta.title()).isEqualTo(expectedTitle);
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
