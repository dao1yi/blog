package com.xmq.mapper;

import com.xmq.entity.Article;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xmq.model.vo.ArticleHotVO;
import org.apache.ibatis.annotations.Param;
import java.util.List;

/**
* @author xumengqi
* @description 针对表【article(文章表)】的数据库操作Mapper
* @createDate 2025-02-25 22:22:23
* @Entity com.xmq.entity.Article
*/
public interface ArticleMapper extends BaseMapper<Article> {
    
    /**
     * 查询热门文章列表
     * @param type 统计类型（view/like）
     * @param days 统计天数
     * @param limit 获取条数
     * @return 热门文章列表
     */
    List<ArticleHotVO> selectHotArticles(
        @Param("type") String type,
        @Param("days") int days,
        @Param("limit") int limit
    );
}




