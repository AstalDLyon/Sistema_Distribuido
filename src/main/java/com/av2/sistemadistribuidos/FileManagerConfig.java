package com.av2.sistemadistribuidos;

public class FileManagerConfig {
    private final String filePath;
    private final int maxFileSize;
    private final String encoding;
    private final int maxBackups;

    public FileManagerConfig(String filePath) {
        this(filePath, 10_000_000, "UTF-8", 3);
    }

    public FileManagerConfig(String filePath, int maxFileSize, String encoding, int maxBackups) {
        this.filePath = filePath;
        this.maxFileSize = maxFileSize;
        this.encoding = encoding;
        this.maxBackups = maxBackups;
    }

    public String getFilePath() { return filePath; }
    public int getMaxFileSize() { return maxFileSize; }
    public String getEncoding() { return encoding; }
    public int getMaxBackups() { return maxBackups; }
}