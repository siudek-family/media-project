package net.siudek.media;

import java.nio.file.Path;

/// All recognized types of directories and files, allowed to be a part of Media assets.  
public sealed interface Source {
    sealed interface Dir extends Source {}
    sealed interface File extends Source {}
    record RootDir(Path value, Dir source, Path target) implements Source {}

    record MediaDir(Iterable<Dir> subdirs, Iterable<File> files) implements Dir {}
    record GitDir(Path value) implements Dir {}
    record DvdDir(Path value) implements Dir {}

    record JpgFile(Path value) implements File {}
    record YmlFile(Path value) implements File {}
    record JsonFile(Path value) implements File {}
    record PngFile(Path value) implements File {}
    record PdfFile(Path value) implements File {}
    
    /** Audio file. */
    record AmrFile(Path value) implements File {}

    record GitignoreFile(Path value) implements File {}

    record M4aFile(Path value) implements File {}
    record MkvFile(Path value) implements File {}
    record Mp4File(Path value) implements File {}
    record DngFile(Path value) implements File {}
    record MovFile(Path value) implements File {}
    record AviFile(Path value) implements File {}
    record WavFile(Path value) implements File {}
    record DocxFile(Path value) implements File {}
    record RagFile(Path value) implements File {}
    record Vid3gpFile(Path value) implements File {}
    record GifFile(Path value) implements File {}
    record NpoFile(Path value) implements File {}
    record NarFile(Path value) implements File {}
    record MpoFile(Path value) implements File {}
    record HeicFile(Path value) implements File {}
    record Mp3File(Path value) implements File {}
}
