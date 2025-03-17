package com.xmq.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.xmq.annotation.NeedLogin;
import com.xmq.common.Result;
import com.xmq.entity.Article;
import com.xmq.service.LikeRecordService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/articles")
@RequiredArgsConstructor
public class LikeController {

    private final LikeRecordService likeRecordService;

    @GetMapping("/{articleId}/like/status")
    @NeedLogin
    public Result<Boolean> getLikeStatus(@PathVariable Long articleId) {
        return Result.success(likeRecordService.getLikeStatus(articleId));
    }

    @PostMapping("/{articleId}/like")
    @NeedLogin
    public Result<Void> toggleLike(@PathVariable Long articleId) {
        likeRecordService.toggleLike(articleId);
        return Result.success();
    }

    @GetMapping("/users/likes")
    @NeedLogin
    public Result<IPage<Article>> getLikedArticles(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return Result.success(likeRecordService.getLikedArticles(page, size));
    }
} 