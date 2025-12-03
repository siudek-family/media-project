package net.siudek.media;

import java.nio.file.Path;
import java.util.Optional;

public final class Sources {
    
    private Sources() {
        // utility class
    }

    public static Source of(Path path) {
        var maybeIsRootDir = isRootDir(path.getParent());
        if (maybeIsRootDir.isPresent()) {
            return maybeIsRootDir.get();
        }
        throw new IllegalArgumentException("Unsupported source directory: " + path);
    }

    private static Optional<Source.RootDir> isRootDir(Path path) {
        final String docs = ".docs";
        final Optional<Source.RootDir> noResult = Optional.empty();
        if (!path.resolve(docs).toFile().exists()) {
            return noResult;
        }
        final String project = ".project";
        if (!path.resolve(project).toFile().exists()) {
            return noResult;
        }
        var source = path.resolve("source");
        if (!source.toFile().exists()) {
            return noResult;
        }
        var target = path.resolve("target");
        if (!target.toFile().exists()) {
            return noResult;
        }
        var result = new Source.RootDir(path, source, target);
        return Optional.of(result);
    }

}
