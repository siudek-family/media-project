package net.siudek.media;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest
@TestPropertySource(properties = {
	"spring.shell.interactive.enabled=false"
})
class MediaTest {

    @Autowired
    Media media;

    @MockitoBean
    TestCommandsListener listener = new TestCommandsListener();

    @Test
    void verifyNameConvention_shouldRenameFileWithUnderscoreToHyphen() {
        // Given
        var path = Path.of("20230115_143022.jpg");
        
        // When
        media.verifyNameConvention(path);
        
        // Then
        assertThat(listener.commands).hasSize(1);
        var command = (MediaCommands.RenameMediaItem) listener.commands.getFirst();
        assertThat(command.from()).isEqualTo(path);
        assertThat(command.newName()).isEqualTo("20230115-143022.jpg");
    }

    @Test
    void verifyNameConvention_shouldHandleFileWithMultipleDots() {
        // Given
        var path = Path.of("20240101_235959.backup.jpg");
        
        // When
        media.verifyNameConvention(path);
        
        // Then
        assertThat(listener.commands).hasSize(1);
        var command = (MediaCommands.RenameMediaItem) listener.commands.getFirst();
        assertThat(command.newName()).isEqualTo("20240101-235959.backup.jpg");
    }

    @Test
    void verifyNameConvention_shouldThrowExceptionForInvalidFormat() {
        // Given
        var path = Path.of("invalid_filename.jpg");
        
        // When & Then
        assertThatThrownBy(() -> media.verifyNameConvention(path))
                .isInstanceOf(UnsupportedOperationException.class)
                .hasMessageContaining("Not implemented yet");
        
        assertThat(listener.commands).isEmpty();
    }

    private static class TestCommandsListener implements CommandsListener {
        final List<MediaCommands> commands = new ArrayList<>();

        @Override
        public void on(MediaCommands command) {
            commands.add(command);
        }
    }
}
