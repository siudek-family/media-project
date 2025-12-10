package net.siudek.media;

import java.nio.file.Path;

/** All supported types of directories, allowed to be a part of Media sources. */
public sealed interface Source {
    sealed interface Dir extends Source {}
    sealed interface File extends Source {}
    record RootDir(Path value, Dir source, Path target) implements Source {}

    record MediaDir(Iterable<MediaDir> subdirs, Iterable<File> files) implements Dir {}
    record GitDir(Path value) implements Dir {}

    record JpgFile(Path value) implements File {}
    record YmlFile(Path value) implements File {}
    record JsonFile(Path value) implements File {}
    record PngFile(Path value) implements File {}
    record PdfFile(Path value) implements File {}
    record AmrFile(Path value) implements File {}
    record TxtFile(Path value) implements File {}
    record GitignoreFile(Path value) implements File {}
    record M4aFile(Path value) implements File {}
    record MkvFile(Path value) implements File {}
}
