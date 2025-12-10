package net.siudek.media;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

import lombok.SneakyThrows;

public final class Sources {
    
    private Sources() {
        // utility class
    }

    public static Source of(Path path) {
        var maybeIsRootDir = isRootDir(path);
        if (maybeIsRootDir.isPresent()) {
            return maybeIsRootDir.get();
        }
        var maybeIsGitDir = isGitRepository(path);
        if (maybeIsGitDir.isPresent()) {
            return maybeIsGitDir.get();
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
        if (!source.toFile().exists() && !source.toFile().isDirectory()) {
            return noResult;
        }
        var target = path.resolve("target");
        if (!target.toFile().exists()) {
            return noResult;
        }
        var sourceDir = asMediaDir(source);
        var result = new Source.RootDir(path, sourceDir, target);
        return Optional.of(result);
    }

    public static Optional<Source.GitDir> isGitRepository(Path path) {
        if (path == null) {
            return Optional.empty();
        }
        var dirName = path.getName(path.getNameCount()-1);
        var isGit = dirName.toString().equals(".git") && path.toFile().isDirectory();
        return isGit ? Optional.of(new Source.GitDir(path)) : Optional.empty();
    }
    public static Optional<Source.DvdDir> isDvdDirectory(Path path) {
        if (path == null) {
            return Optional.empty();
        }
        // if dir contains file 'VIDEO_TS.BUP' then it is a DVD directory
        var isDvd = path.resolve("VIDEO_TS.BUP").toFile().exists() || path.resolve("VIDEO_RM.BUP").toFile().exists();
        return isDvd ? Optional.of(new Source.DvdDir(path)) : Optional.empty();
    }

    @SneakyThrows(java.io.IOException.class)
    static Source.Dir asMediaDir(Path path) {

      if (isGitRepository(path).isPresent()) {
          return new Source.GitDir(path);
      }
      if (isDvdDirectory(path).isPresent()) {
        return new Source.DvdDir(path);
      }

      var subdirs = Files.list(path)
          .filter(Files::isDirectory)
          .map(p -> {
            var a = switch (asMediaDir(p)) {
                case Source.MediaDir md -> md;
                case Source.GitDir it -> null;
                case Source.DvdDir _ -> null;
            };
            return a;
          })
          .filter(it -> it != null)
          .toList();
      var files = Files.list(path)
          .filter(p -> !Files.isDirectory(p))
          .map(p -> asFile(p))
          .toList();
        return new Source.MediaDir(subdirs, files);
    }

    static Source.File asFile(Path path) {
        var fileName = path.getFileName().toString().toLowerCase();
        if (fileName.endsWith(".jpg") || fileName.endsWith(".jpeg")) {
            return new Source.JpgFile(path);
        }
        if (fileName.endsWith(".yml") || fileName.endsWith(".yaml")) {
            return new Source.YmlFile(path);
        }
        if (fileName.endsWith(".json")) {
            return new Source.JsonFile(path);
        }
        if (fileName.endsWith(".png")) {
            return new Source.PngFile(path);
        }
        if (fileName.endsWith(".pdf")) {
            return new Source.PdfFile(path);
        }
        if (fileName.endsWith(".amr")) {
            return new Source.AmrFile(path);
        }
        if (fileName.endsWith(".txt")) {
            return new Source.TxtFile(path);
        }
        if (fileName.endsWith(".gitignore")) {
            return new Source.GitignoreFile(path);
        }
        if (fileName.endsWith(".m4a")) {
            return new Source.M4aFile(path);
        }
        if (fileName.endsWith(".mkv")) {
            return new Source.MkvFile(path);
        }
        if (fileName.endsWith(".mp4")) {
            return new Source.Mp4File(path);
        }
        if (fileName.endsWith(".dng")) {
            return new Source.DngFile(path);
        }
        if (fileName.endsWith(".mov")) {
            return new Source.MovFile(path);
        }
        if (fileName.endsWith(".avi")) {
            return new Source.AviFile(path);
        }
        if (fileName.endsWith(".wav")) {
            return new Source.WavFile(path);
        }
        if (fileName.endsWith(".docx")) {
            return new Source.DocxFile(path);
        }
        if (fileName.endsWith(".rag")) {
            return new Source.RagFile(path);
        }
        if (fileName.endsWith(".3gp")) {
            return new Source.Vid3gpFile(path);
        }
        if (fileName.endsWith(".gif")) {
            return new Source.GifFile(path);
        }
        if (fileName.endsWith(".npo")) {
            return new Source.NpoFile(path);
        }
        if (fileName.endsWith(".nar")) {
            return new Source.NarFile(path);
        }
        if (fileName.endsWith(".mpo")) {
            return new Source.MpoFile(path);
        }
        if (fileName.endsWith(".heic")) {
            return new Source.HeicFile(path);
        }
        if (fileName.endsWith(".mp3")) {
            return new Source.Mp3File(path);
        }
        throw new IllegalArgumentException("Unsupported file type: " + path);
    }
    
}
