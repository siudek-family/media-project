package net.siudek.media;

import java.util.HashSet;
import java.util.Set;

public class Media {
    
    public static Set<MediaItem> toMedia(Source.RootDir rootDir) {
        var result = new HashSet<MediaItem>();

        process(rootDir, result);

        return result;
    }

    static void process(Source.RootDir source, Set<MediaItem> result) {
        
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
    
    static void process(Source.MediaDir mediaDir, Set<MediaItem> result) {
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
        for (var file : mediaDir.files()) {
            switch (file) {
                case Source.JpgFile jpgFile -> {
                    // TODO process
                }
                case Source.Mp4File mp4File -> {
                    // TODO process
                }
                case Source.MkvFile mkvFile -> {
                    // TODO process
                }
                case Source.Mp3File mp3File -> {
                    // TODO process
                }
                case Source.PngFile pngFile -> {
                    // TODO process
                }
                case Source.PdfFile pdfFile -> {
                    // TODO process
                }
                case Source.YmlFile ymlFile -> {
                    // TODO process
                }
                case Source.JsonFile jsonFile -> {
                    // TODO process
                }
                case Source.AmrFile amrFile -> {
                    // TODO process
                }
                case Source.TxtFile txtFile -> {
                    throw new IllegalStateException("TXT files should not be present in media directories: " + txtFile.value());
                }
                case Source.GitignoreFile gitignoreFile -> {
                    // TODO process
                }
                case Source.M4aFile m4aFile -> {
                    // TODO process
                }
                case Source.DngFile dngFile -> {
                    // TODO process
                }
                case Source.MovFile movFile -> {
                    // TODO process
                }
                case Source.AviFile aviFile -> {
                    // TODO process
                }
                case Source.WavFile wavFile -> {
                    // TODO process
                }
                case Source.DocxFile docxFile -> {
                    // TODO process
                }
                case Source.RagFile ragFile -> {
                    // TODO process
                }
                case Source.Vid3gpFile vid3gpFile -> {
                    // TODO process
                }
                case Source.GifFile gifFile -> {
                    // TODO process
                }
                case Source.NpoFile npoFile -> {
                    // TODO process
                }
                case Source.NarFile narFile -> {
                    // TODO process
                }
                case Source.MpoFile mpoFile -> {
                    // TODO process
                }
                case Source.HeicFile heicFile -> {
                    // TODO process
                }
            }
        }
    }

}
