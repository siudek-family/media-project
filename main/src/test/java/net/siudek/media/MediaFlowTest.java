package net.siudek.media;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import net.siudek.media.rename.RenameStrategy;

class MediaFlowTest {

    @Test
    void verifyNameConvention_shouldUseMatchingStrategyAndEmitCommand() {
        // Given
        var path = Path.of("20130508.jpg");
        var commandsListener = mock(CommandsListener.class);
        var strategy = mock(RenameStrategy.class);
        var cmd = new MediaCommands.RenameMediaItem(
            path,
            new MediaCommands.GenericMeta(LocalDateTime.of(2013, 5, 8, 0, 0, 0), "jpg", path));

        when(strategy.tryRename(path)).thenReturn(Optional.of(cmd));

        var media = new Media(List.of(strategy), commandsListener);

        // When
        media.verifyNameConvention(path);

        // Then
        verify(strategy).tryRename(path);
        verify(commandsListener).on(cmd);
    }

    @Test
    void verifyNameConvention_shouldThrowWhenNoStrategyMatches() {
        // Given
        var path = Path.of("20130508.jpg");
        var commandsListener = mock(CommandsListener.class);
        var strategy = mock(RenameStrategy.class);

        when(strategy.tryRename(path)).thenReturn(Optional.empty());

        var media = new Media(List.of(strategy), commandsListener);

        // When & Then
        assertThatThrownBy(() -> media.verifyNameConvention(path))
            .isInstanceOf(UnsupportedOperationException.class)
            .hasMessageContaining("Not implemented yet");
    }

}