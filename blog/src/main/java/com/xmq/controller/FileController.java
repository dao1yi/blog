package com.xmq.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xmq.annotation.RequireAdmin;
import com.xmq.common.Result;
import com.xmq.entity.FileUpload;
import com.xmq.service.FileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.HashMap;
import java.util.Map;

@Tag(name = "文件管理")
@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
public class FileController {
    
    private final FileService fileService;
    
    @Operation(summary = "上传文件")
    @PostMapping()
    @RequireAdmin
    public Result<?> uploadFile(@RequestParam("file") MultipartFile file) {
        String fileUrl = fileService.uploadFile(file);
        Map<String, String> data = new HashMap<>();
        data.put("url", fileUrl);
        return Result.success(data);
    }
    
    @Operation(summary = "删除文件")
    @DeleteMapping("/{id}")
    @RequireAdmin
    public Result<?> deleteFile(@PathVariable Long id) {
        fileService.deleteFile(id);
        return Result.success();
    }
    
    @Operation(summary = "获取文件列表")
    @GetMapping
    @RequireAdmin
    public Result<?> getFileList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String fileName,
            @RequestParam(required = false) String fileType) {
        Page<FileUpload> fileList = fileService.getFileList(page, size, fileName, fileType);
        return Result.success(fileList);
    }
} 