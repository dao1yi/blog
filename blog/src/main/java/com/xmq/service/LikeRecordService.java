package com.xmq.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.xmq.entity.Article;
import com.xmq.entity.LikeRecord;
import com.baomidou.mybatisplus.extension.service.IService;

/**
* @author xumengqi
* @description 针对表【like_record(文章点赞记录表)】的数据库操作Service
* @createDate 2025-02-25 22:22:23
*/
public interface LikeRecordService extends IService<LikeRecord> {

    /**
     * 获取用户对文章的点赞状态
     * @param articleId 文章ID
     * @return 是否已点赞
     */
    boolean getLikeStatus(Long articleId);

    /**
     * 点赞或取消点赞文章
     * @param articleId 文章ID
     */
    void toggleLike(Long articleId);

    /**
     * 获取用户点赞的文章列表
     * @param page 页码
     * @param size 每页大小
     * @return 分页的文章列表
     */
    IPage<Article> getLikedArticles(int page, int size);
}
