package com.xmq.controller;

import com.xmq.annotation.RequireAdmin;
import com.xmq.common.Result;
import com.xmq.model.entity.Resume;
import com.xmq.service.ResumeService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/resume")
@RequiredArgsConstructor
@Tag(name = "简历管理", description = "简历相关接口")
public class ResumeController {

    private final ResumeService resumeService;

    @Operation(summary = "保存简历", description = "创建新的简历信息")
    @PostMapping
    @RequireAdmin
    public Result<Void> saveResume(@RequestBody Resume resume) {
        resumeService.saveResume(resume);
        return Result.success();
    }

    @Operation(summary = "获取简历", description = "获取当前简历信息")
    @GetMapping
    public Result<Resume> getResume() {
        Resume resume = resumeService.getResume();
        return Result.success(resume);
    }

    @Operation(summary = "删除简历", description = "删除现有简历信息")
    @DeleteMapping
    @RequireAdmin
    public Result<Void> deleteResume() {
        resumeService.deleteResume();
        return Result.success();
    }

    @Operation(summary = "更新简历", description = "更新现有简历信息")
    @PutMapping
    @RequireAdmin
    public Result<Void> updateResume(@RequestBody Resume resume) {
        resumeService.updateResume(resume);
        return Result.success();
    }
} 