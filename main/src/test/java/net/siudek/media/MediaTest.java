package net.siudek.media;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest(classes = {Program.class})
@Import(MediaTest.TestConfig.class)
@TestPropertySource(properties = {
	"spring.shell.interactive.enabled=false"
})
class MediaTest {

    @Autowired
    Media media;

    @Autowired
    TestCommandsListener listener;

    @AfterEach
    void tearDown() {
        listener.command = null;
    }
    
    @Test
    void verifyNameConvention_shouldRenameFileWithUnderscoreToHyphen() {
        // Given
        var path = Path.of("20230115_143022.jpg");
        
        // When
        media.verifyNameConvention(path);
        
        // Then
        var command = (MediaCommands.RenameMediaItem) listener.command;
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
        var command = (MediaCommands.RenameMediaItem) listener.command;
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
        
        assertThat(listener.command).isNull();
    }

    private static class TestCommandsListener implements CommandsListener {
        MediaCommands command = null;

        @Override
        public void on(MediaCommands command) {
            this.command = command;
        }
    }

    @Configuration
    static class TestConfig {
        @Bean
        @Primary
        TestCommandsListener testCommandsListener() {
            return new TestCommandsListener();
        }
    }
}
