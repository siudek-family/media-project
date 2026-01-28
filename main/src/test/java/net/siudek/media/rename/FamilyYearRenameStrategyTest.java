package net.siudek.media.rename;

import net.siudek.media.MediaCommands;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.time.Year;

import static org.assertj.core.api.Assertions.assertThat;

class FamilyYearRenameStrategyTest {

    private final FamilyYearRenameStrategy strategy = new FamilyYearRenameStrategy();

    @Test
    void shouldMatchFileInRodzinneFolderWithYear() {
        // given
        var path = Path.of("x:/media/Rodzinne/2023/Family photo.jpg");

        // when
        var result = strategy.tryRename(path);

        // then
        assertThat(result).isPresent();
        var command = (MediaCommands.RenameMediaItem) result.get();
        var meta = (MediaCommands.GenericMetaYear) command.meta();
        
        assertThat(meta.date()).isEqualTo(Year.of(2023));
        assertThat(meta.content()).isEqualTo("Family photo");
        assertThat(meta.extension()).isEqualTo("jpg");
        assertThat(meta.location()).isEqualTo(path);
    }

    @Test
    void shouldMatchCaseInsensitiveRodzinneFolder() {
        // given
        var path = Path.of("x:/media/rodzinne/2023/photo.jpg");

        // when
        var result = strategy.tryRename(path);

        // then
        assertThat(result).isPresent();
        var command = (MediaCommands.RenameMediaItem) result.get();
        var meta = (MediaCommands.GenericMetaYear) command.meta();
        
        assertThat(meta.date()).isEqualTo(Year.of(2023));
        assertThat(meta.content()).isEqualTo("photo");
        assertThat(meta.extension()).isEqualTo("jpg");
    }

    @Test
    void shouldMatchMixedCaseRodzinneFolder() {
        // given
        var path = Path.of("x:/media/RoDzInNe/2024/vacation.png");

        // when
        var result = strategy.tryRename(path);

        // then
        assertThat(result).isPresent();
        var command = (MediaCommands.RenameMediaItem) result.get();
        var meta = (MediaCommands.GenericMetaYear) command.meta();
        
        assertThat(meta.date()).isEqualTo(Year.of(2024));
        assertThat(meta.content()).isEqualTo("vacation");
        assertThat(meta.extension()).isEqualTo("png");
    }

    @Test
    void shouldNotMatchFilenameStartingWithDigit() {
        // given
        var path = Path.of("x:/media/Rodzinne/2023/2023-photo.jpg");

        // when
        var result = strategy.tryRename(path);

        // then
        assertThat(result).isEmpty();
    }

    @Test
    void shouldNotMatchWrongFolderStructure() {
        // given
        var path = Path.of("x:/media/Other/2023/photo.jpg");

        // when
        var result = strategy.tryRename(path);

        // then
        assertThat(result).isEmpty();
    }

    @Test
    void shouldNotMatchNestedSubfolder() {
        // given
        var path = Path.of("x:/media/Rodzinne/2023/subfolder/photo.jpg");

        // when
        var result = strategy.tryRename(path);

        // then
        assertThat(result).isEmpty();
    }

    @Test
    void shouldNotMatchInvalidYearFolder() {
        // given
        var path = Path.of("x:/media/Rodzinne/abc/photo.jpg");

        // when
        var result = strategy.tryRename(path);

        // then
        assertThat(result).isEmpty();
    }

    @Test
    void shouldNotMatchFileWithoutExtension() {
        // given
        var path = Path.of("x:/media/Rodzinne/2023/photo");

        // when
        var result = strategy.tryRename(path);

        // then
        assertThat(result).isEmpty();
    }

    @Test
    void shouldNotMatchRootFile() {
        // given
        var path = Path.of("x:/photo.jpg");

        // when
        var result = strategy.tryRename(path);

        // then
        assertThat(result).isEmpty();
    }

    @Test
    void shouldMatchDifferentYears() {
        // given
        var path2020 = Path.of("x:/media/Rodzinne/2020/old photo.jpg");
        var path2025 = Path.of("x:/media/Rodzinne/2025/new photo.jpg");

        // when
        var result2020 = strategy.tryRename(path2020);
        var result2025 = strategy.tryRename(path2025);

        // then
        assertThat(result2020).isPresent();
        assertThat(result2025).isPresent();
        
        var meta2020 = (MediaCommands.GenericMetaYear) ((MediaCommands.RenameMediaItem) result2020.get()).meta();
        var meta2025 = (MediaCommands.GenericMetaYear) ((MediaCommands.RenameMediaItem) result2025.get()).meta();
        
        assertThat(meta2020.date()).isEqualTo(Year.of(2020));
        assertThat(meta2025.date()).isEqualTo(Year.of(2025));
    }

    @Test
    void shouldExtractContentWithoutExtension() {
        // given
        var path = Path.of("x:/media/Rodzinne/2023/My family photo summer.jpg");

        // when
        var result = strategy.tryRename(path);

        // then
        assertThat(result).isPresent();
        var meta = (MediaCommands.GenericMetaYear) ((MediaCommands.RenameMediaItem) result.get()).meta();
        
        assertThat(meta.content()).isEqualTo("My family photo summer");
        assertThat(meta.extension()).isEqualTo("jpg");
    }
}
