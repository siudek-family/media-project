package net.siudek.media.shell;

import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;

import net.siudek.media.Source;
import net.siudek.media.Sources;

@ShellComponent
public class HelloWorldCommands {

    AtomicInteger counter = new AtomicInteger(0);

    // shell Start method 
    @ShellMethod(value = "Start the shell application", key = "start")
    public String start() {
        var currentPath = Path.of("").toAbsolutePath();
        
        var rootProject = currentPath.getParent().getParent();
        var rootDir = Sources.of(rootProject);

        switch (rootDir) {
            case Source.RootDir it -> act(it);
            default -> throw new IllegalStateException("Unsupported root dir: " + rootDir);
        };

        return "The end.";
    }

    void act(Source.RootDir dir) {
        var sourceDir = dir.source();
        act(sourceDir);
    }

    void act(Source.Dir dir) {
        switch (dir) {
            case Source.MediaDir md -> act(md);
            case Source.GitDir gd -> act(gd);
            case Source.DvdDir dd -> act(dd);
        };
    }

    void act(Source.MediaDir dir) {
        for (var subdir : dir.subdirs()) {
            System.out.println("Subdir: " + subdir);
            act(subdir);
        };
        for (var file : dir.files()) {
            System.out.println("File: " + file);
            act(file);
        };

    }

    void act(Source.GitDir dir) {
        // TODO implement
    }

    void act(Source.DvdDir dir) {
        // TODO implement
    }

    void act(Source.File file) {
        // TODO implement
    }



}
