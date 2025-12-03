package net.siudek.media;

import java.nio.file.Path;

/** All supported types of directories, allowed to be a part of Media sources. */
public sealed interface Source {
    
    record RootDir(Path value, Path source, Path target) implements Source {}
    sealed interface Dir extends Source {}
    sealed interface File extends Source {}

    record MediaDir(Iterable<Dir> subdirs, Iterable<File> files) implements Dir {}
    record JpgFile(Path value) implements File {}
}
