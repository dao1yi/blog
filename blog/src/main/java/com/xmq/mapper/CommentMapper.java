package com.xmq.mapper;

import com.xmq.entity.Comment;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.MapKey;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
* @author xumengqi
* @description 针对表【comment(评论表)】的数据库操作Mapper
* @createDate 2025-02-25 22:22:23
* @Entity com.xmq.entity.Comment
*/
public interface CommentMapper extends BaseMapper<Comment> {
    
    /**
     * 批量查询评论的子评论数量
     * @param commentIds 评论ID列表
     * @return 评论ID -> 子评论数量的映射
     */
    @MapKey("parent_id")
    Map<Long,Map<String, Long>> selectChildCount(@Param("commentIds") List<Long> commentIds);

    void updateChildComments(Long id, Long parentId, Date date);
}




