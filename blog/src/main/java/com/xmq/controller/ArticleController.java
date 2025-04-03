package com.xmq.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xmq.annotation.RequireAdmin;
import com.xmq.common.Result;
import com.xmq.common.ResultCode;
import com.xmq.entity.Article;
import com.xmq.enums.ArticleHotType;
import com.xmq.enums.PeriodType;
import com.xmq.exception.BusinessException;
import com.xmq.model.dto.ArticleCreateDTO;
import com.xmq.model.dto.ArticleQueryDTO;
import com.xmq.model.vo.ArticleHotVO;
import com.xmq.service.ArticleService;
import com.xmq.service.ViewRecordService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "文章管理", description = "文章相关接口")
@RestController
@RequestMapping("/api/articles")
@RequiredArgsConstructor
public class ArticleController {

    private final ArticleService articleService;

    private final ViewRecordService viewRecordService;

    @Operation(summary = "创建文章", description = "创建新的文章")
    @PostMapping
    @RequireAdmin
    public Result<Void> createArticle(@RequestBody ArticleCreateDTO articleCreateDTO) {
        articleService.createArticle(articleCreateDTO);
        return Result.success();
    }

    @Operation(summary = "获取文章列表", description = "分页获取文章列表")
    @GetMapping
    public Result<IPage<Article>> getArticleList(ArticleQueryDTO queryDTO) {
        IPage<Article> page = articleService.getArticleList(queryDTO);
        return Result.success(page);
    }
    

    @PutMapping("/{id}")
    @RequireAdmin
    @Operation(summary = "更新文章", description = "更新文章")
    public Result<?> updateArticle(@PathVariable Long id, @RequestBody Article article) {
        article.setId(id);
        articleService.updateArticle(article);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    @RequireAdmin
    @Operation(summary = "删除文章", description = "根据文章id删除文章")
    public Result<?> deleteArticle(@PathVariable Long id) {
        articleService.deleteArticle(id);
        return Result.success();
    }

    @Operation(summary = "记录文章浏览")
    @PostMapping("/{id}/view")
    public Result<?> recordView(@PathVariable("id") Long articleId,
                              @RequestHeader(value = "X-Forwarded-For", required = false) String forwardedIp,
                              HttpServletRequest request) {
        // 获取真实IP地址
        String ipAddress = forwardedIp != null ? forwardedIp.split(",")[0].trim() 
                                              : request.getRemoteAddr();
        
        viewRecordService.recordView(articleId, ipAddress);
        return Result.success();
    }

    @GetMapping("/hot")
    @Operation(summary = "获取热门文章", description = "分页获取热门文章")
    public Result<List<ArticleHotVO>> getHotArticles(
            @RequestParam String type,
            @RequestParam String period,
            @RequestParam(defaultValue = "10") int limit) {
        
        // 参数校验
        ArticleHotType hotType = ArticleHotType.fromCode(type);
        if (hotType == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR);
        }

        PeriodType periodType = PeriodType.fromCode(period);
        if (periodType == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR);
        }

        if (limit <= 0 || limit > 50) {
            limit = 10;
        }

        return Result.success(articleService.getHotArticles(hotType, periodType, limit));
    }

    @Operation(summary = "获取文章详情", description = "根据文章ID获取文章详细信息")
    @GetMapping("/{id}")
    public Result<Article> getArticleDetail(@PathVariable("id") Long id) {
        Article article = articleService.getArticleDetail(id);
        return Result.success(article);
    }
} 