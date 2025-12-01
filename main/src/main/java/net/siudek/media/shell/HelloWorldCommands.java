package net.siudek.media.shell;

import java.nio.file.Path;

import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;

import net.siudek.media.Source;
import net.siudek.media.Sources;

@ShellComponent
public class HelloWorldCommands {

    // shell Start method 
    @ShellMethod(value = "Start the shell application", key = "start")
    public String start() {
        var currentPath = Path.of("").toAbsolutePath();
        

        var rootDir = Sources.of(currentPath.getParent());

        var typed = switch (rootDir) {
            case Source.RootDir r -> r;
        };

        return "Hello, Media Shell! Current source: " + typed;
    }

}
