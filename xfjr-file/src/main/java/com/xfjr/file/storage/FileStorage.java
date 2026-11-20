package com.xfjr.file.storage;

import java.io.IOException;

/**
 * 文件存储后端抽象，支持本地 / MinIO / OSS 可插拔实现
 */
public interface FileStorage {

    String store(String fileId, String originalName, byte[] content) throws IOException;

    byte[] retrieve(String storagePath) throws IOException;

    void remove(String storagePath) throws IOException;
}
