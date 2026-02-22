package net.siudek.media.rename;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Path;
import java.time.LocalDate;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import net.siudek.media.MediaCommands;

@DisplayName("GenericDateOnlyRenameStrategy")
class GenericDateOnlyRenameStrategyTest {

    private final GenericDateOnlyRenameStrategy strategy = new GenericDateOnlyRenameStrategy();

    @Test
    @DisplayName("should rename file with yyyyMMdd pattern using date-only metadata")
    void shouldRenameFileWithDateOnlyPattern(@TempDir Path tempDir) {
        var filePath = tempDir.resolve("20130508.jpg");

        var result = strategy.tryRename(filePath);

        assertThat(result).isPresent();
        var command = (MediaCommands.RenameMediaItem) result.get();
        assertThat(command.from()).isEqualTo(filePath);

        var meta = (MediaCommands.GenericMetaYMD) command.meta();
        assertThat(meta.date()).isEqualTo(LocalDate.of(2013, 5, 8));
        assertThat(meta.extension()).isEqualTo("jpg");
        assertThat(meta.location()).isEqualTo(filePath);
        assertThat(MediaCommands.asFilename(meta)).isEqualTo("20130508.jpg");
    }

    @Test
    @DisplayName("should return empty Optional for non-matching file pattern")
    void shouldReturnEmptyForNonMatchingPattern(@TempDir Path tempDir) {
        var filePath = tempDir.resolve("20130508-120000.jpg");

        var result = strategy.tryRename(filePath);

        assertThat(result).isEmpty();
    }
}