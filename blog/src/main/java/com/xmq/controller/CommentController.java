package com.xmq.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xmq.annotation.NeedLogin;
import com.xmq.annotation.RequireAdmin;
import com.xmq.common.Result;
import com.xmq.model.dto.CommentCreateDTO;
import com.xmq.model.dto.CommentQueryDTO;
import com.xmq.model.vo.CommentVO;
import com.xmq.service.CommentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "评论管理")
@RestController
@RequestMapping("/api/articles/{articleId}/comments")
@RequiredArgsConstructor
public class CommentController {
    
    private final CommentService commentService;
    
    @Operation(summary = "发表评论")
    @PostMapping
    @NeedLogin
    public Result<?> createComment(@PathVariable Long articleId, 
                                 @Valid @RequestBody CommentCreateDTO commentDTO) {
        commentDTO.setArticleId(articleId);
        commentService.createComment(commentDTO);
        return Result.success();
    }


    
    @Operation(summary = "获取评论列表")
    @GetMapping
    public Result<?> getCommentList(@PathVariable Long articleId,
                                   @RequestParam(required = false) Long parentId,
                                   @RequestParam(defaultValue = "1") Integer page,
                                   @RequestParam(defaultValue = "10") Integer size) {
        CommentQueryDTO queryDTO = new CommentQueryDTO();
        queryDTO.setArticleId(articleId);
        queryDTO.setParentId(parentId);
        queryDTO.setPage(page);
        queryDTO.setSize(size);
        
        Page<CommentVO> commentList = commentService.getCommentList(queryDTO);
        return Result.success(commentList);
    }

    @Operation(summary = "删除评论")
    @DeleteMapping("/{id}")
    @NeedLogin
    public Result<?> deleteComment(@PathVariable Long articleId,@PathVariable Long id) {
        commentService.deleteComment(id);
        return Result.success();
    }

    @Operation(summary = "改变置顶状态")
    @PutMapping("/{id}")
    @RequireAdmin
    public Result<?> changeTopStatus(@PathVariable Long articleId,
                                     @PathVariable Long id) {
        commentService.changeTopStatus(id);
        return Result.success();
    }
} 