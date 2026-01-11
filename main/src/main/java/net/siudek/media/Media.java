package net.siudek.media;

import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.siudek.media.rename.RenameStrategy;

@Slf4j
@RequiredArgsConstructor
@Component
public class Media {
    
    private final CommandsListener commandsListener;
    private final Set<RenameStrategy> renameStrategies;

    public Set<MediaItem> toMedia(Source.RootDir rootDir) {
        var result = new HashSet<MediaItem>();

        process(rootDir, result);

        return result;
    }

    void process(Source.RootDir source, Set<MediaItem> result) {
        
        switch (source.source()) {
            case Source.MediaDir mediaDir -> {
                // process media dir
                process(mediaDir, result);
            }
            case Source.GitDir gitDir -> {
                // TODO
            }
            case Source.DvdDir dvdDir -> {
                // TODO: verify if DVD has related a single video file
            }
        }
    }
    
    void process(Source.MediaDir mediaDir, Set<MediaItem> result) {
        for (var dir : mediaDir.subdirs()) {
            switch (dir) {
                case Source.MediaDir subMediaDir -> {
                    process(subMediaDir, result);
                }
                case Source.GitDir gitDir -> {
                    // process git dir
                }
                case Source.DvdDir dvdDir -> {
                    // process dvd dir
                }
            }
        }
        for (var mediaFile : mediaDir.files()) {
            switch (mediaFile) {
                case Source.JpgFile file -> {
                    verifyNameConvention(file.value());
                }
                case Source.Mp4File mp4File -> {
                    log.info("TODO: Processing MP4 file: {}", mp4File.value());
                }
                case Source.MkvFile mkvFile -> {
                    log.info("TODO: Processing MKV file: {}", mkvFile.value());
                }
                case Source.Mp3File mp3File -> {
                    log.info("TODO: Processing MP3 file: {}", mp3File.value());
                }
                case Source.PngFile pngFile -> {
                    log.info("TODO: Processing PNG file: {}", pngFile.value());
                }
                case Source.PdfFile pdfFile -> {
                    log.info("TODO: Processing PDF file: {}", pdfFile.value());
                }
                case Source.YmlFile ymlFile -> {
                    log.info("TODO: Processing YML file: {}", ymlFile.value());
                }
                case Source.JsonFile jsonFile -> {
                    log.info("TODO: Processing JSON file: {}", jsonFile.value());
                }
                case Source.AmrFile file -> {
                    verifyNameConvention(file.value());
                }
                case Source.GitignoreFile gitignoreFile -> {
                    log.info("TODO: Processing GITIGNORE file: {}", gitignoreFile.value());
                }
                case Source.M4aFile m4aFile -> {
                    log.info("TODO: Processing M4A file: {}", m4aFile.value());
                }
                case Source.DngFile dngFile -> {
                    log.info("TODO: Processing DNG file: {}", dngFile.value());
                }
                case Source.MovFile movFile -> {
                    log.info("TODO: Processing MOV file: {}", movFile.value());
                }
                case Source.AviFile aviFile -> {
                    log.info("TODO: Processing AVI file: {}", aviFile.value());
                }
                case Source.WavFile wavFile -> {
                    log.info("TODO: Processing WAV file: {}", wavFile.value());
                }
                case Source.DocxFile docxFile -> {
                    throw new IllegalStateException("DOCX files should not be present in media directories: " + docxFile.value());
                }
                case Source.RagFile ragFile -> {
                    log.info("TODO: Processing RAG file: {}", ragFile.value());
                }
                case Source.Vid3gpFile vid3gpFile -> {
                    log.info("TODO: Processing 3GP file: {}", vid3gpFile.value());
                }
                case Source.GifFile gifFile -> {
                    log.info("TODO: Processing GIF file: {}", gifFile.value());
                }
                case Source.NpoFile npoFile -> {
                    log.info("TODO: Processing NPO file: {}", npoFile.value());
                }
                case Source.NarFile narFile -> {
                    log.info("TODO: Processing NAR file: {}", narFile.value());
                }
                case Source.MpoFile mpoFile -> {
                    log.info("TODO: Processing MPO file: {}", mpoFile.value());
                }
                case Source.HeicFile heicFile -> {
                    log.info("TODO: Processing HEIC file: {}", heicFile.value());
                }
            }
        }
    }

    /// Filenames should be defined in form of yyyyMMdd-hhmmss.
    /// If name is different, we should try to define conversion method of its current name to proper one.
    public void verifyNameConvention(Path value) {

        var validStrategies = renameStrategies.stream().filter(rs -> rs.tryRename(value)).toList();
        if (validStrategies.size() > 1) {
            throw new IllegalStateException("Multiple rename strategies matched for file: " + value);
        }
        if (validStrategies.size() == 1) {
            return;
        }

        // TODO when attribute "creationTime" is available, and that attribute is same as year / month updirectory, we should rename the file
        // in other case, we should throw an exception
        throw new UnsupportedOperationException("Not implemented yet: verifyNameConvention for " + value);
    }

}
