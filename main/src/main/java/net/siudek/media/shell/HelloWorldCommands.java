package net.siudek.media.shell;

import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;

import net.siudek.media.Media;
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

        var media = switch (rootDir) {
            case Source.RootDir it -> {
                yield Media.toMedia(it);
            }
            default -> throw new IllegalStateException("Unsupported root dir: " + rootDir);
        };

        return "The end.";
    }

}
