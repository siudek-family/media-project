package net.siudek.media;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class SourcesTest {

    @Test
    void shouldReturnGitDirWhenFolderContainsGitDirectory(@TempDir Path tempDir) throws IOException {
        // given
        var gitDir = tempDir.resolve(".git");
        Files.createDirectory(gitDir);

        // when
        var result = Sources.isGitRepository(gitDir);

        // then
        assertTrue(result.isPresent());
        assertEquals(gitDir, result.get().value());
    }

    @Test
    void shouldReturnEmptyWhenFolderDoesNotContainGitDirectory(@TempDir Path tempDir) {
        // when
        var result = Sources.isGitRepository(tempDir);

        // then
        assertTrue(result.isEmpty());
    }

    @Test
    void shouldReturnEmptyWhenGitIsFileNotDirectory(@TempDir Path tempDir) throws IOException {
        // given
        var gitFile = tempDir.resolve(".git");
        Files.createFile(gitFile);

        // when
        var result = Sources.isGitRepository(gitFile);

        // then
        assertTrue(result.isEmpty());
    }

    @Test
    void shouldReturnEmptyWhenPathIsNull() {
        // when
        var result = Sources.isGitRepository(null);

        // then
        assertTrue(result.isEmpty());
    }
}
