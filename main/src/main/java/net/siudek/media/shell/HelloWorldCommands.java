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

        switch (rootDir) {
            case Source.RootDir it -> act(it);
            case Source.Dir it -> act(it);
            case Source.File it -> act(it);
        };

        return "The end.";
    }

    @SneakyThrows(java.io.IOException.class)
    void act(Source.RootDir dir) {
        var source = dir.source();
        Files.walk(source)
            .filter(it -> !Files.isDirectory(it))
            .peek(it -> Sources.of(it))
            .count();
    }

    void act(Source.Dir dir) {
        // TODO implement
    }

    void act(Source.File file) {
        // TODO implement
    }



}
