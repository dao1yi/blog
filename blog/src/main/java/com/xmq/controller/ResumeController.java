package com.xmq.controller;

import com.xmq.annotation.RequireAdmin;
import com.xmq.common.Result;
import com.xmq.model.entity.Resume;
import com.xmq.service.ResumeService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/resume")
@RequiredArgsConstructor
public class ResumeController {

    private final ResumeService resumeService;

    @PostMapping
    @RequireAdmin
    public Result<Void> saveResume(@RequestBody Resume resume) {
        resumeService.saveResume(resume);
        return Result.success();
    }

    @GetMapping
    public Result<Resume> getResume() {
        Resume resume = resumeService.getResume();
        return Result.success(resume);
    }

    @DeleteMapping
    @RequireAdmin
    public Result<Void> deleteResume() {
        resumeService.deleteResume();
        return Result.success();
    }

    @PutMapping
    @RequireAdmin
    public Result<Void> updateResume(@RequestBody Resume resume) {
        resumeService.updateResume(resume);
        return Result.success();
    }
} 