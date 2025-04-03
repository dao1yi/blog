package com.xmq.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xmq.common.ResultCode;
import com.xmq.entity.Article;
import com.xmq.entity.LikeRecord;
import com.xmq.exception.BusinessException;
import com.xmq.mapper.LikeRecordMapper;
import com.xmq.service.ArticleService;
import com.xmq.service.LikeRecordService;
import com.xmq.util.AuthUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
* @author xumengqi
* @description 针对表【like_record(文章点赞记录表)】的数据库操作Service实现
* @createDate 2025-02-25 22:22:23
*/
@Service
@RequiredArgsConstructor
public class LikeRecordServiceImpl extends ServiceImpl<LikeRecordMapper, LikeRecord>
    implements LikeRecordService {

    private final ArticleService articleService;

    /**
     * 点赞状态：已点赞
     */
    private static final int STATUS_LIKED = 1;
    
    /**
     * 点赞状态：已取消
     */
    private static final int STATUS_CANCELED = 0;

    @Override
    public boolean getLikeStatus(Long articleId) {
        // 检查文章是否存在
        Article article = articleService.getById(articleId);
        if (article == null) {
            throw new BusinessException(ResultCode.ARTICLE_NOT_FOUND);
        }

        // 获取当前用户ID
        Long currentUserId = AuthUtils.getCurrentUserId();

        // 查询点赞记录
        LambdaQueryWrapper<LikeRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(LikeRecord::getArticleId, articleId)
               .eq(LikeRecord::getUserId, currentUserId)
               .eq(LikeRecord::getStatus, STATUS_LIKED);
        
        return this.count(wrapper) > 0;
    }

    @Override
    @Transactional
    public void toggleLike(Long articleId) {
        // 检查文章是否存在
        Article article = articleService.getById(articleId);
        if (article == null) {
            throw new BusinessException(ResultCode.ARTICLE_NOT_FOUND);
        }

        // 获取当前用户ID
        Long currentUserId = AuthUtils.getCurrentUserId();

        // 查询是否已点赞
        LambdaQueryWrapper<LikeRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(LikeRecord::getArticleId, articleId)
               .eq(LikeRecord::getUserId, currentUserId);
        
        LikeRecord likeRecord = this.getOne(wrapper);

        if (likeRecord == null) {
            // 首次点赞，创建记录
            likeRecord = new LikeRecord();
            likeRecord.setArticleId(articleId);
            likeRecord.setUserId(currentUserId);
            likeRecord.setLikeTime(new Date());
            likeRecord.setStatus(STATUS_LIKED);
            this.save(likeRecord);
            
            // 更新文章点赞数
            articleService.updateLikeCount(articleId, true);
        } else if (likeRecord.getStatus() == STATUS_LIKED) {
            // 已点赞，取消点赞
            likeRecord.setStatus(STATUS_CANCELED);
            likeRecord.setLikeTime(new Date());  // 更新操作时间
            this.updateById(likeRecord);
            
            // 更新文章点赞数
            articleService.updateLikeCount(articleId, false);
        } else {
            // 已取消，重新点赞
            likeRecord.setStatus(STATUS_LIKED);
            likeRecord.setLikeTime(new Date());  // 更新操作时间
            this.updateById(likeRecord);
            
            // 更新文章点赞数
            articleService.updateLikeCount(articleId, true);
        }
    }

    @Override
    public IPage<Article> getLikedArticles(int page, int size) {
        // 获取当前用户ID
        Long currentUserId = AuthUtils.getCurrentUserId();

        // 分页查询点赞记录
        Page<LikeRecord> recordPage = new Page<>(page, size);
        LambdaQueryWrapper<LikeRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(LikeRecord::getUserId, currentUserId)
               .eq(LikeRecord::getStatus, STATUS_LIKED)
               .orderByDesc(LikeRecord::getLikeTime);
        
        Page<LikeRecord> likeRecords = this.page(recordPage, wrapper);

        Page<Article> articlePage = new Page<>(
                likeRecords.getCurrent(),
                likeRecords.getSize(),
                likeRecords.getTotal()
        );

        if(likeRecords.getTotal()==0){
            articlePage.setRecords(null);
        }else{
            // 获取文章ID列表
            List<Long> articleIds = likeRecords.getRecords().stream()
                    .map(LikeRecord::getArticleId)
                    .collect(Collectors.toList());

            // 查询文章信息

            LambdaQueryWrapper<Article> articleLambdaQueryWrapper = new LambdaQueryWrapper<>();
            articleLambdaQueryWrapper.eq(Article::getStatus,1);
            articleLambdaQueryWrapper.in(Article::getId,articleIds);
            List<Article> articles = articleService.list(articleLambdaQueryWrapper);

            // 构建返回结果

            articlePage.setRecords(articles);
        }

        return articlePage;
    }
}




