package com.xfjr.file.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xfjr.api.file.FileMeta;
import com.xfjr.common.domain.BusinessException;
import com.xfjr.file.common.FileErrorCode;
import com.xfjr.file.entity.FileObject;
import com.xfjr.file.mapper.FileObjectMapper;
import com.xfjr.file.storage.FileStorage;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class FileObjectService {

    private final FileObjectMapper fileObjectMapper;
    private final FileStorage fileStorage;

    @Value("#{'${file.whitelist:image/jpeg,image/png,image/gif,application/pdf,text/plain}'.split(',')}")
    private List<String> whitelist;
    @Value("${file.max-size:10485760}")
    private Long maxSize;

    public FileObjectService(FileObjectMapper fileObjectMapper, FileStorage fileStorage) {
        this.fileObjectMapper = fileObjectMapper;
        this.fileStorage = fileStorage;
    }

    public FileMeta upload(MultipartFile file, String ownerId, String bizType) {
        String contentType = file.getContentType();
        if (contentType == null || !whitelist.contains(contentType)) {
            throw new BusinessException(FileErrorCode.E_FILE_120101, "不支持的文件类型: " + contentType);
        }
        if (file.getSize() > maxSize) {
            throw new BusinessException(FileErrorCode.E_FILE_120102, "文件大小超过上限: " + file.getSize());
        }
        String fileId = UUID.randomUUID().toString().replace("-", "");
        byte[] content;
        try {
            content = file.getBytes();
        } catch (Exception e) {
            throw new BusinessException(FileErrorCode.E_FILE_120105, "读取上传文件失败");
        }
        String storagePath;
        try {
            storagePath = fileStorage.store(fileId, file.getOriginalFilename(), content);
        } catch (Exception e) {
            throw new BusinessException(FileErrorCode.E_FILE_120105, "文件存储失败");
        }
        LocalDateTime now = LocalDateTime.now();
        FileObject entity = new FileObject();
        entity.setFileId(fileId);
        entity.setOriginalName(file.getOriginalFilename());
        entity.setContentType(contentType);
        entity.setFileSize(file.getSize());
        entity.setStorageType("LOCAL");
        entity.setStoragePath(storagePath);
        entity.setOwnerId(ownerId);
        entity.setBizType(bizType);
        entity.setStatus("VALID");
        entity.setCreateDate(now);
        entity.setUpdateDate(now);
        fileObjectMapper.insert(entity);
        return toMeta(entity);
    }

    public FileMeta metadata(String fileId) {
        return toMeta(findByFileId(fileId));
    }

    public byte[] download(String fileId) {
        FileObject entity = findByFileId(fileId);
        try {
            return fileStorage.retrieve(entity.getStoragePath());
        } catch (Exception e) {
            throw new BusinessException(FileErrorCode.E_FILE_120103, "文件读取失败");
        }
    }

    public void delete(String fileId) {
        FileObject entity = findByFileId(fileId);
        entity.setStatus("DELETED");
        entity.setUpdateDate(LocalDateTime.now());
        fileObjectMapper.updateById(entity);
        try {
            fileStorage.remove(entity.getStoragePath());
        } catch (Exception ignored) {
        }
    }

    private FileObject findByFileId(String fileId) {
        FileObject entity = fileObjectMapper.selectOne(
                new LambdaQueryWrapper<FileObject>().eq(FileObject::getFileId, fileId));
        if (entity == null) {
            throw new BusinessException(FileErrorCode.E_FILE_120103, "文件不存在: " + fileId);
        }
        if ("DELETED".equals(entity.getStatus())) {
            throw new BusinessException(FileErrorCode.E_FILE_120104, "文件已删除: " + fileId);
        }
        return entity;
    }

    private FileMeta toMeta(FileObject entity) {
        FileMeta meta = new FileMeta();
        BeanUtils.copyProperties(entity, meta);
        return meta;
    }
}
