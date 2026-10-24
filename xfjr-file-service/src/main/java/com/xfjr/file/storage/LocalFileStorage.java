package com.xfjr.file.storage;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * 本地文件系统存储实现（生产可替换为 MinIO / OSS 实现）
 */
@Component
public class LocalFileStorage implements FileStorage {

    @Value("${file.storage.local-path:/tmp/xfjr-file}")
    private String basePath;

    @Override
    public String store(String fileId, String originalName, byte[] content) throws IOException {
        Path dir = Paths.get(basePath);
        if (!Files.exists(dir)) {
            Files.createDirectories(dir);
        }
        String safeName = fileId.replaceAll("[^a-zA-Z0-9_-]", "_");
        Path target = dir.resolve(safeName);
        Files.write(target, content);
        return target.toString();
    }

    @Override
    public byte[] retrieve(String storagePath) throws IOException {
        return Files.readAllBytes(Paths.get(storagePath));
    }

    @Override
    public void remove(String storagePath) throws IOException {
        Files.deleteIfExists(Paths.get(storagePath));
    }
}
