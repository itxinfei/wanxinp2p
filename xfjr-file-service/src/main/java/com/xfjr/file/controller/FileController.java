package com.xfjr.file.controller;

import com.xfjr.file.service.FileObjectService;
import com.xfjr.api.file.FileAPI;
import com.xfjr.common.domain.RestResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = "File服务API")
public class FileController implements FileAPI {

    @Autowired
    private FileObjectService fileObjectService;

    @Operation(summary = "服务健康检查")
    @GetMapping("/l/file/health")
    public RestResponse<String> health() {
        return RestResponse.success(fileObjectService.ping());
    }
}
