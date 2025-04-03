package com.xmq.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xmq.common.ResultCode;
import com.xmq.entity.Article;
import com.xmq.entity.CollectionRecord;
import com.xmq.exception.BusinessException;
import com.xmq.mapper.CollectionRecordMapper;
import com.xmq.service.ArticleService;
import com.xmq.service.CollectionRecordService;
import com.xmq.util.AuthUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
* @author xumengqi
* @description 针对表【collection_record(文章收藏记录表)】的数据库操作Service实现
* @createDate 2025-02-25 22:22:23
*/
@Service
@RequiredArgsConstructor
public class CollectionRecordServiceImpl extends ServiceImpl<CollectionRecordMapper, CollectionRecord>
    implements CollectionRecordService {

    private final ArticleService articleService;

    /**
     * 收藏状态：已收藏
     */
    private static final int STATUS_COLLECTED = 1;
    
    /**
     * 收藏状态：已取消
     */
    private static final int STATUS_CANCELED = 0;

    @Override
    public boolean getCollectionStatus(Long articleId) {
        // 检查文章是否存在
        Article article = articleService.getById(articleId);
        if (article == null) {
            throw new BusinessException(ResultCode.ARTICLE_NOT_FOUND);
        }

        // 获取当前用户ID
        Long currentUserId = AuthUtils.getCurrentUserId();

        // 查询收藏记录
        LambdaQueryWrapper<CollectionRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CollectionRecord::getArticleId, articleId)
               .eq(CollectionRecord::getUserId, currentUserId)
               .eq(CollectionRecord::getStatus, STATUS_COLLECTED);
        
        return this.count(wrapper) > 0;
    }

    @Override
    @Transactional
    public void toggleCollection(Long articleId) {
        // 检查文章是否存在
        Article article = articleService.getById(articleId);
        if (article == null) {
            throw new BusinessException(ResultCode.ARTICLE_NOT_FOUND);
        }

        // 获取当前用户ID
        Long currentUserId = AuthUtils.getCurrentUserId();

        // 查询是否已收藏
        LambdaQueryWrapper<CollectionRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CollectionRecord::getArticleId, articleId)
               .eq(CollectionRecord::getUserId, currentUserId);
        
        CollectionRecord collectionRecord = this.getOne(wrapper);

        if (collectionRecord == null) {
            // 首次收藏，创建记录
            collectionRecord = new CollectionRecord();
            collectionRecord.setArticleId(articleId);
            collectionRecord.setUserId(currentUserId);
            collectionRecord.setCollectionTime(new Date());
            collectionRecord.setStatus(STATUS_COLLECTED);
            this.save(collectionRecord);
            
            // 更新文章收藏数
            articleService.updateCollectionCount(articleId, true);
        } else if (collectionRecord.getStatus() == STATUS_COLLECTED) {
            // 已收藏，取消收藏
            collectionRecord.setStatus(STATUS_CANCELED);
            collectionRecord.setCollectionTime(new Date());  // 更新操作时间
            this.updateById(collectionRecord);
            
            // 更新文章收藏数
            articleService.updateCollectionCount(articleId, false);
        } else {
            // 已取消，重新收藏
            collectionRecord.setStatus(STATUS_COLLECTED);
            collectionRecord.setCollectionTime(new Date());  // 更新操作时间
            this.updateById(collectionRecord);
            
            // 更新文章收藏数
            articleService.updateCollectionCount(articleId, true);
        }
    }

    @Override
    public IPage<Article> getCollectedArticles(int page, int size) {
        // 获取当前用户ID
        Long currentUserId = AuthUtils.getCurrentUserId();

        // 分页查询收藏记录
        Page<CollectionRecord> recordPage = new Page<>(page, size);
        LambdaQueryWrapper<CollectionRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CollectionRecord::getUserId, currentUserId)
               .eq(CollectionRecord::getStatus, STATUS_COLLECTED)
               .orderByDesc(CollectionRecord::getCollectionTime);
        
        Page<CollectionRecord> collectionRecords = this.page(recordPage, wrapper);

        Page<Article> articlePage = new Page<>(
                collectionRecords.getCurrent(),
                collectionRecords.getSize(),
                collectionRecords.getTotal()
        );
        if(collectionRecords.getTotal()==0){
            articlePage.setRecords(null);
        }else{
            // 获取文章ID列表
            List<Long> articleIds = collectionRecords.getRecords().stream()
                    .map(CollectionRecord::getArticleId)
                    .collect(Collectors.toList());
            // 查询文章信息

            LambdaQueryWrapper<Article> articleLambdaQueryWrapper = new LambdaQueryWrapper<>();
            articleLambdaQueryWrapper.eq(Article::getStatus,1);
            articleLambdaQueryWrapper.in(Article::getId,articleIds);
            List<Article> articles = articleService.list(articleLambdaQueryWrapper);
            articlePage.setRecords(articles);
        }
        return articlePage;
    }
}




