package com.xmq.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.xmq.annotation.NeedLogin;
import com.xmq.annotation.RequireAdmin;
import com.xmq.common.Result;
import com.xmq.entity.Article;
import com.xmq.service.CollectionRecordService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/articles")
@RequiredArgsConstructor
public class CollectionController {

    private final CollectionRecordService collectionRecordService;

    @GetMapping("/{articleId}/collection/status")
    @NeedLogin
    public Result<Boolean> getCollectionStatus(@PathVariable Long articleId) {
        return Result.success(collectionRecordService.getCollectionStatus(articleId));
    }

    @PostMapping("/{articleId}/collection")
    @NeedLogin
    public Result<Void> toggleCollection(@PathVariable Long articleId) {
        collectionRecordService.toggleCollection(articleId);
        return Result.success();
    }

    @GetMapping("/users/collections")
    @NeedLogin
    public Result<IPage<Article>> getCollectedArticles(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return Result.success(collectionRecordService.getCollectedArticles(page, size));
    }
} 