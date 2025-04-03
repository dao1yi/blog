package com.xmq.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.xmq.entity.Article;
import com.xmq.model.dto.ArticleCreateDTO;
import com.xmq.model.dto.ArticleQueryDTO;
import com.xmq.enums.ArticleHotType;
import com.xmq.enums.PeriodType;
import com.xmq.model.vo.ArticleHotVO;
import java.util.List;

/**
* @author xumengqi
* @description 针对表【article(文章表)】的数据库操作Service
* @createDate 2025-02-25 22:22:23
*/
public interface ArticleService extends IService<Article> {
    /**
     * 创建文章
     * @param articleCreateDTO 创建文章请求
     */
    void createArticle(ArticleCreateDTO articleCreateDTO);

    /**
     * 获取文章列表
     * @param queryDTO 查询参数
     * @return 分页文章列表
     */
    IPage<Article> getArticleList(ArticleQueryDTO queryDTO);

    /**
     * 修改文章
     * @param article 文章信息
     */
    void updateArticle(Article article);

    /**
     * 删除文章
     * @param id 文章ID
     */
    void deleteArticle(Long id);

    /**
     * 增加文章浏览量
     * @param articleId 文章ID
     */
    void incrementViewCount(Long articleId);

    /**
     * 更新文章评论数
     * @param articleId 文章ID
     * @param increment true为增加，false为减少
     */
    void updateCommentCount(Long articleId, boolean increment);

    /**
     * 更新文章点赞数
     * @param articleId 文章ID
     * @param increment true为增加，false为减少
     */
    void updateLikeCount(Long articleId, boolean increment);

    /**
     * 更新文章收藏数
     * @param articleId 文章ID
     * @param increment true为增加，false为减少
     */
    void updateCollectionCount(Long articleId, boolean increment);

    /**
     * 根据文章ID列表批量查询文章
     * @param articleIds 文章ID列表
     * @return 文章列表，按照ID列表顺序返回
     */
    List<Article> getArticlesByIds(List<Long> articleIds);

    /**
     * 获取热门文章列表
     * @param type 统计类型（浏览量/点赞量）
     * @param period 统计周期（日/周/月）
     * @param limit 获取条数
     * @return 热门文章列表
     */
    List<ArticleHotVO> getHotArticles(ArticleHotType type, PeriodType period, int limit);

    /**
     * 获取文章详情
     * @param id 文章ID
     * @return 文章详情
     */
    Article getArticleDetail(Long id);
}
