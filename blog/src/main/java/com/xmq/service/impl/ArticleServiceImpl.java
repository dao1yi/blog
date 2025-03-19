package com.xmq.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.xmq.common.ResultCode;
import com.xmq.constant.RedisConstants;
import com.xmq.entity.Article;
import com.xmq.enums.ArticleHotType;
import com.xmq.enums.PeriodType;
import com.xmq.exception.BusinessException;
import com.xmq.mapper.ArticleMapper;
import com.xmq.model.dto.ArticleCreateDTO;
import com.xmq.model.dto.ArticleQueryDTO;
import com.xmq.model.vo.ArticleHotVO;
import com.xmq.service.ArticleService;
import com.xmq.util.AuthUtils;
import jakarta.annotation.Resource;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
* @author xumengqi
* @description 针对表【article(文章表)】的数据库操作Service实现
* @createDate 2025-02-25 22:22:23
*/
@Service
public class ArticleServiceImpl extends ServiceImpl<ArticleMapper, Article>
    implements ArticleService{

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private ObjectMapper objectMapper;
    @Override
    public void createArticle(ArticleCreateDTO articleCreateDTO) {
        // 1. 获取当前用户ID
        Long userId = AuthUtils.getCurrentUserId();

        // 2. 构建文章对象
        Article article = new Article();
        article.setTitle(articleCreateDTO.getTitle());
        article.setContent(articleCreateDTO.getContent());
        article.setSummary(articleCreateDTO.getSummary());
        article.setCoverImage(articleCreateDTO.getCoverImage());
        article.setStatus(articleCreateDTO.getStatus());
        article.setAuthorId(userId);
        article.setViewCount(0);
        article.setCommentCount(0);

        // 3. 如果没有提供摘要，自动生成摘要
//        if (!StringUtils.hasText(article.getSummary())) {
//            String content = article.getContent();
//            int summaryLength = Math.min(content.length(), 500);
//            article.setSummary(content.substring(0, summaryLength));
//        }

        // 4. 保存文章
        save(article);
    }

    @Override
    public IPage<Article> getArticleList(ArticleQueryDTO queryDTO) {
        // 1. 构建分页对象
        Page<Article> page = new Page<>(queryDTO.getPage(), queryDTO.getSize());
        
        // 2. 构建查询条件
        LambdaQueryWrapper<Article> wrapper = new LambdaQueryWrapper<>();
        
        // 2.1 标题关键字查询
        wrapper.like(StringUtils.hasText(queryDTO.getKeyword()), 
            Article::getTitle, queryDTO.getKeyword());
        
        // 2.2 状态查询
        Long currentUserId = null;
        try {
            currentUserId = AuthUtils.getCurrentUserId();
        } catch (BusinessException e) {
            // 未登录用户，只能看已发布的文章
            wrapper.eq(Article::getStatus, 1);
        }
        
        if (currentUserId != null) {
            if (queryDTO.getStatus() != null) {
                // 已登录用户，根据状态查询
                if (queryDTO.getStatus() == 0) {
                    // 草稿只能看自己的
                    wrapper.eq(Article::getStatus, 0)
                          .eq(Article::getAuthorId, currentUserId);
                } else {
                    // 已发布的所有人都能看
                    wrapper.eq(Article::getStatus, 1);
                }
            } else {
                // 状态为空时，显示所有已发布的和自己的草稿
                Long finalCurrentUserId = currentUserId;
//                wrapper.and(w -> w.eq(Article::getStatus, 1)
//                        .or()
//                        .eq(Article::getAuthorId, finalCurrentUserId));
//                wrapper.and(w -> w.eq(Article::getAuthorId, finalCurrentUserId));
                wrapper.eq(Article::getAuthorId, finalCurrentUserId);
            }
        }
        
        // 2.3 过滤已删除的文章
        wrapper.eq(Article::getIsDeleted, 0);
        
        // 2.4 按创建时间倒序
        wrapper.orderByDesc(Article::getCreatedTime);
        
        // 3. 执行查询
        return page(page, wrapper);
    }

    @Override
    public void updateArticle(Article article) {
        // 检查文章是否存在
        Article existingArticle = this.getById(article.getId());
        if (existingArticle == null) {
            throw new BusinessException(ResultCode.ARTICLE_NOT_FOUND);
        }
        
        // 更新文章
        article.setUpdatedTime(new Date());
        boolean success = this.updateById(article);
        if (!success) {
            throw new BusinessException(ResultCode.ARTICLE_UPDATE_ERROR);
        }
    }

    @Override
    public void deleteArticle(Long id) {
        // 检查文章是否存在
        Article article = this.getById(id);
        if (article == null) {
            throw new BusinessException(ResultCode.ARTICLE_NOT_FOUND);
        }
        
        // 逻辑删除文章
        article.setIsDeleted(1);
        article.setUpdatedTime(new Date());
        boolean success = this.updateById(article);
        if (!success) {
            throw new BusinessException(ResultCode.ARTICLE_DELETE_ERROR);
        }
    }

    @Override
    @Transactional
    public void incrementViewCount(Long articleId) {
        // 使用SQL直接更新浏览量，避免并发问题
        boolean success = this.update()
                .setSql("view_count = view_count + 1")
                .eq("id", articleId)
                .update();
        
        if (!success) {
            throw new BusinessException(ResultCode.ARTICLE_UPDATE_ERROR);
        }
    }

    @Override
    @Transactional
    public void updateCommentCount(Long articleId, boolean increment) {
        // 使用SQL直接更新评论数
        boolean success = this.update()
                .setSql("comment_count = comment_count " + (increment ? "+ 1" : "- 1"))
                .eq("id", articleId)
                .ge(increment ? "1=1" : "comment_count", 1) // 确保减少时评论数不会小于0
                .update();
        
        if (!success) {
            throw new BusinessException(ResultCode.ARTICLE_UPDATE_ERROR);
        }
    }

    @Override
    @Transactional
    public void updateLikeCount(Long articleId, boolean increment) {
        // 使用SQL直接更新点赞数
        boolean success = this.update()
                .setSql("like_count = like_count " + (increment ? "+ 1" : "- 1"))
                .eq("id", articleId)
                .ge(increment ? "1=1" : "like_count", 1) // 确保减少时点赞数不会小于0
                .update();
        
        if (!success) {
            throw new BusinessException(ResultCode.ARTICLE_UPDATE_ERROR);
        }
    }

    @Override
    @Transactional
    public void updateCollectionCount(Long articleId, boolean increment) {
        // 使用SQL直接更新收藏数
        boolean success = this.update()
                .setSql("collection_count = collection_count " + (increment ? "+ 1" : "- 1"))
                .eq("id", articleId)
                .ge(increment ? "1=1" : "collection_count", 1) // 确保减少时收藏数不会小于0
                .update();
        
        if (!success) {
            throw new BusinessException(ResultCode.ARTICLE_UPDATE_ERROR);
        }
    }

    @Override
    public List<Article> getArticlesByIds(List<Long> articleIds) {
        if (articleIds == null || articleIds.isEmpty()) {
            return List.of();
        }

        // 批量查询文章
        List<Article> articles = this.listByIds(articleIds);
        // 将查询结果转换为Map，以便按照输入的ID顺序重新排序
        Map<Long, Article> articleMap = articles.stream()
            .collect(Collectors.toMap(Article::getId, article -> article));
        
        // 按照输入的ID顺序返回文章列表
        return articleIds.stream()
            .map(articleMap::get)
            .filter(article -> article != null)
            .collect(Collectors.toList());
    }

    @Override
    public List<ArticleHotVO> getHotArticles(ArticleHotType type, PeriodType period, int limit) {
        String s = stringRedisTemplate.opsForValue().get(RedisConstants.HOT_ARTICLES_PREFIX + type + ":" + period);
        if(s!=null){
            List<ArticleHotVO> articleHotVOs = null;
            try {
                articleHotVOs = objectMapper.readValue(s, List.class);
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
            return articleHotVOs;
        }

        List<ArticleHotVO> articleHotVOS = this.baseMapper.selectHotArticles(type.getCode(), period.getDays(), limit);
        try {
            stringRedisTemplate.opsForValue().set(RedisConstants.HOT_ARTICLES_PREFIX + type + ":" + period,objectMapper.writeValueAsString(articleHotVOS),30L, TimeUnit.MINUTES);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        return articleHotVOS;
    }

    @Override
    public Article getArticleDetail(Long id) {
        // 获取文章详情
        Article article = this.getById(id);
        
        // 文章不存在则抛出异常
        if (article == null || article.getIsDeleted() == 1) {
            throw new BusinessException(ResultCode.ARTICLE_NOT_FOUND);
        }
        
        // 返回文章详情
        return article;
    }
}




