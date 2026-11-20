package com.xfjr.file.controller;

import com.xfjr.api.file.FileAPI;
import com.xfjr.api.file.FileMeta;
import com.xfjr.common.domain.RestResponse;
import com.xfjr.file.service.FileObjectService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/l/files")
@Tag(name = "File服务API")
public class FileController implements FileAPI {

    @Autowired
    private FileObjectService fileObjectService;

    @Operation(summary = "服务健康检查")
    @GetMapping("/health")
    public RestResponse<String> health() {
        return RestResponse.success("File service alive");
    }

    @Operation(summary = "上传文件（Multipart）")
    @PostMapping
    public RestResponse<FileMeta> upload(@RequestParam("file") MultipartFile file,
                                         @RequestParam("ownerId") String ownerId,
                                         @RequestParam(value = "bizType", required = false) String bizType) {
        return RestResponse.success(fileObjectService.upload(file, ownerId, bizType));
    }

    @Operation(summary = "查询文件元数据")
    @GetMapping("/{fileId}")
    public RestResponse<FileMeta> metadata(@PathVariable String fileId) {
        return RestResponse.success(fileObjectService.metadata(fileId));
    }

    @Operation(summary = "下载文件原始内容")
    @GetMapping("/{fileId}/raw")
    public ResponseEntity<byte[]> raw(@PathVariable String fileId) {
        FileMeta meta = fileObjectService.metadata(fileId);
        byte[] content = fileObjectService.download(fileId);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_TYPE, meta.getContentType())
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + meta.getOriginalName() + "\"")
                .body(content);
    }

    @Operation(summary = "删除文件")
    @DeleteMapping("/{fileId}")
    public RestResponse<Void> delete(@PathVariable String fileId) {
        fileObjectService.delete(fileId);
        return RestResponse.success();
    }
}
