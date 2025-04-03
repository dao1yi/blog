package com.xmq.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.xmq.entity.Comment;
import com.xmq.model.dto.CommentCreateDTO;
import com.xmq.model.dto.CommentQueryDTO;
import com.xmq.model.vo.CommentVO;

/**
* @author xumengqi
* @description 针对表【comment(评论表)】的数据库操作Service
* @createDate 2025-02-25 22:22:23
*/
public interface CommentService extends IService<Comment> {

    /**
     * 发表评论
     * @param commentDTO 评论信息
     */
    void createComment(CommentCreateDTO commentDTO);

    /**
     * 获取评论列表
     * @param queryDTO 查询条件
     * @return 评论列表
     */
    Page<CommentVO> getCommentList(CommentQueryDTO queryDTO);

    /**
     * 删除评论
     * @param id 评论ID
     */
    void deleteComment(Long id);

    void changeTopStatus(Long id);
}
