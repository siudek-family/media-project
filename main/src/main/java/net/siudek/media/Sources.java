package net.siudek.media;

import java.nio.file.Path;
import java.util.Optional;

public final class Sources {
    
    private Sources() {
        // utility class
    }

    public static Source of(Path path) {
        var maybeIsRootDir = isRootDir(path);
        if (maybeIsRootDir.isPresent()) {
            return maybeIsRootDir.get();
        }
        throw new IllegalArgumentException("Unsupported source directory: " + path);
    }

    private static Optional<Source.RootDir> isRootDir(Path path) {
        final String docs = ".docs";
        final Optional<Source.RootDir> noResult = Optional.empty();
        if (path.resolve(docs).toFile().exists()) {
            return noResult;
        }
        final String project = ".project";
        if (path.resolve(project).toFile().exists()) {
            return noResult;
        }
        final String source = "source";
        if (path.resolve(source).toFile().exists()) {
            return noResult;
        }
        final String target = "target";
        if (path.resolve(target).toFile().exists()) {
            return noResult;
        }
        var result = new Source.RootDir(path);
        return Optional.of(result);
    }

}
