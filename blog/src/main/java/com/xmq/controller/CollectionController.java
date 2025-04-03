package com.xmq.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.xmq.annotation.NeedLogin;
import com.xmq.annotation.RequireAdmin;
import com.xmq.common.Result;
import com.xmq.entity.Article;
import com.xmq.service.CollectionRecordService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/articles")
@RequiredArgsConstructor
@Tag(name = "收藏管理", description = "文章收藏相关接口")
public class CollectionController {

    private final CollectionRecordService collectionRecordService;

    @Operation(summary = "获取收藏状态", description = "获取当前用户是否收藏了指定文章")
    @GetMapping("/{articleId}/collection/status")
    @NeedLogin
    public Result<Boolean> getCollectionStatus(@PathVariable Long articleId) {
        return Result.success(collectionRecordService.getCollectionStatus(articleId));
    }

    @Operation(summary = "切换收藏状态", description = "收藏或取消收藏文章")
    @PostMapping("/{articleId}/collection")
    @NeedLogin
    public Result<Void> toggleCollection(@PathVariable Long articleId) {
        collectionRecordService.toggleCollection(articleId);
        return Result.success();
    }

    @Operation(summary = "获取收藏列表", description = "分页获取当前用户收藏的文章列表")
    @GetMapping("/users/collections")
    @NeedLogin
    public Result<IPage<Article>> getCollectedArticles(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return Result.success(collectionRecordService.getCollectedArticles(page, size));
    }
} 