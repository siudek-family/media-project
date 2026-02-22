package net.siudek.media.rename;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Path;
import java.time.YearMonth;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import net.siudek.media.MediaCommands;

@DisplayName("GenericYearMonthUnknownDayRenameStrategy")
class GenericYearMonthUnknownDayRenameStrategyTest {

    private final GenericYearMonthUnknownDayRenameStrategy strategy = new GenericYearMonthUnknownDayRenameStrategy();

    @Test
    @DisplayName("should rename file with yyyyMM__ pattern using year-month metadata")
    void shouldRenameFileWithYearMonthUnknownDayPattern(@TempDir Path tempDir) {
        var filePath = tempDir.resolve("202105__.jpg");

        var result = strategy.tryRename(filePath);

        assertThat(result).isPresent();
        var command = (MediaCommands.RenameMediaItem) result.get();
        assertThat(command.from()).isEqualTo(filePath);

        var meta = (MediaCommands.GenericMetaYM) command.meta();
        assertThat(meta.date()).isEqualTo(YearMonth.of(2021, 5));
        assertThat(meta.extension()).isEqualTo("jpg");
        assertThat(meta.location()).isEqualTo(filePath);
        assertThat(MediaCommands.asFilename(meta)).isEqualTo("202105__.jpg");
    }

    @Test
    @DisplayName("should return empty Optional for non-matching file pattern")
    void shouldReturnEmptyForNonMatchingPattern(@TempDir Path tempDir) {
        var filePath = tempDir.resolve("20210515.jpg");

        var result = strategy.tryRename(filePath);

        assertThat(result).isEmpty();
    }
}