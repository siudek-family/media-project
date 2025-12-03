package net.siudek.media.shell;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;

import lombok.SneakyThrows;
import net.siudek.media.Source;
import net.siudek.media.Sources;

@ShellComponent
public class HelloWorldCommands {

    AtomicInteger counter = new AtomicInteger(0);

    // shell Start method 
    @ShellMethod(value = "Start the shell application", key = "start")
    public String start() {
        var currentPath = Path.of("").toAbsolutePath();
        
        var rootDir = Sources.of(currentPath.getParent());

        var result = switch (rootDir) {
            case Source.RootDir r -> act(r);
        };

        return "The end. Files: " + result;
    }

    @SneakyThrows(java.io.IOException.class)
    long act(Source.RootDir dir) {
        var source = dir.source();
        return Files.walk(source)
            .filter(it -> !Files.isDirectory(it))
            .peek(it -> Sources.of(it))
            .count();
    }

}
