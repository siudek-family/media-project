package net.siudek.media;

import java.nio.file.Path;

/** All supported types of directories, allowed to be a part of Media sources. */
public sealed interface Source {
    
    record RootDir(Path value) implements Source {}
}
