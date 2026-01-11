package net.siudek.media;

/// Logical set of media assets, build on top of all supported types of phisical directories and files.
public interface MediaItem {
    
    record RootDir() implements MediaItem {}
}
