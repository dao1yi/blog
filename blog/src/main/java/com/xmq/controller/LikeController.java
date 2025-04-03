package com.xmq.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.xmq.annotation.NeedLogin;
import com.xmq.common.Result;
import com.xmq.entity.Article;
import com.xmq.service.LikeRecordService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/articles")
@RequiredArgsConstructor
@Tag(name = "点赞管理", description = "文章点赞相关接口")
public class LikeController {

    private final LikeRecordService likeRecordService;

    @Operation(summary = "获取点赞状态", description = "获取当前用户是否点赞了指定文章")
    @GetMapping("/{articleId}/like/status")
    @NeedLogin
    public Result<Boolean> getLikeStatus(@PathVariable Long articleId) {
        return Result.success(likeRecordService.getLikeStatus(articleId));
    }

    @Operation(summary = "切换点赞状态", description = "点赞或取消点赞文章")
    @PostMapping("/{articleId}/like")
    @NeedLogin
    public Result<Void> toggleLike(@PathVariable Long articleId) {
        likeRecordService.toggleLike(articleId);
        return Result.success();
    }

    @Operation(summary = "获取点赞列表", description = "分页获取当前用户点赞的文章列表")
    @GetMapping("/users/likes")
    @NeedLogin
    public Result<IPage<Article>> getLikedArticles(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return Result.success(likeRecordService.getLikedArticles(page, size));
    }
} 