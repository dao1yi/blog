package com.xmq.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xmq.common.ResultCode;
import com.xmq.entity.Article;
import com.xmq.entity.Comment;
import com.xmq.entity.User;
import com.xmq.exception.BusinessException;
import com.xmq.mapper.CommentMapper;
import com.xmq.model.LoginUser;
import com.xmq.model.dto.CommentCreateDTO;
import com.xmq.model.dto.CommentQueryDTO;
import com.xmq.model.vo.CommentVO;
import com.xmq.service.ArticleService;
import com.xmq.service.CommentService;
import com.xmq.service.UserService;
import com.xmq.util.AuthUtils;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
* @author xumengqi
* @description 针对表【comment(评论表)】的数据库操作Service实现
* @createDate 2025-02-25 22:22:23
*/
@Service
@RequiredArgsConstructor
public class CommentServiceImpl extends ServiceImpl<CommentMapper, Comment>
    implements CommentService{

    private final ArticleService articleService;
    private final UserService userService;

    @Resource
    private CommentMapper commentMapper;
    @Override
    @Transactional
    public void createComment(CommentCreateDTO commentDTO) {
        // 检查文章是否存在
        Article article = articleService.getById(commentDTO.getArticleId());
        if (article == null) {
            throw new BusinessException(ResultCode.ARTICLE_NOT_FOUND);
        }
        
        // 如果有父评论，检查父评论是否存在
        if (commentDTO.getParentId() != null) {
            Comment parentComment = this.getById(commentDTO.getParentId());
            if (parentComment == null) {
                throw new BusinessException(ResultCode.COMMENT_NOT_FOUND);
            }
            // 检查父评论是否属于同一篇文章
            if (!parentComment.getArticleId().equals(commentDTO.getArticleId())) {
                throw new BusinessException(ResultCode.PARAM_ERROR, "父评论不属于该文章");
            }
        }
        
        // 创建评论
        Comment comment = new Comment();
        comment.setContent(commentDTO.getContent());
        comment.setArticleId(commentDTO.getArticleId());
        comment.setParentId(commentDTO.getParentId());
        comment.setUserId(AuthUtils.getCurrentUserId());
        comment.setCreatedTime(new Date());
        comment.setUpdatedTime(new Date());
        
        // 保存评论
        boolean success = this.save(comment);
        if (!success) {
            throw new BusinessException(ResultCode.INTERNAL_ERROR, "评论发表失败");
        }

        // 更新文章评论数
        articleService.updateCommentCount(commentDTO.getArticleId(), true);
    }

    @Override
    public Page<CommentVO> getCommentList(CommentQueryDTO queryDTO) {
        // 检查文章是否存在
        Article article = articleService.getById(queryDTO.getArticleId());
        if (article == null) {
            throw new BusinessException(ResultCode.ARTICLE_NOT_FOUND);
        }
        
        // 如果传入了父评论ID，检查父评论是否存在
        if (queryDTO.getParentId() != null) {
            Comment parentComment = this.getById(queryDTO.getParentId());
            if (parentComment == null) {
                throw new BusinessException(ResultCode.COMMENT_NOT_FOUND);
            }
        }
        
        // 构建查询条件
        LambdaQueryWrapper<Comment> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Comment::getArticleId, queryDTO.getArticleId());
        if(queryDTO.getParentId() != null) {
            wrapper.eq(Comment::getParentId, queryDTO.getParentId());
        }else{
            wrapper.isNull(Comment::getParentId);
        }

        wrapper.orderByDesc(Comment::getCreatedTime);
        wrapper.orderByDesc(Comment::getIsTop);
        
        // 分页查询
        Page<Comment> page = new Page<>(queryDTO.getPage(), queryDTO.getSize());
        Page<Comment> commentPage = this.page(page, wrapper);
        
        // 如果没有数据，直接返回空页
        if (commentPage.getRecords().isEmpty()) {
            return new Page<CommentVO>().setTotal(0);
        }
        
        // 获取评论用户ID列表
        List<Long> userIds = commentPage.getRecords().stream()
                .map(Comment::getUserId)
                .distinct()
                .collect(Collectors.toList());
        
        // 批量查询用户信息
        List<User> users = userService.listByIds(userIds);
        Map<Long, User> userMap = users.stream()
                .collect(Collectors.toMap(User::getId, user -> user));
        
        // 查询每条评论的子评论数量
        List<Long> commentIds = commentPage.getRecords().stream()
                .map(Comment::getId)
                .collect(Collectors.toList());
        
        Map<Long,Map<String, Long>> childCountMap = this.baseMapper.selectChildCount(commentIds);
        System.out.println(childCountMap);
        // 转换为VO对象
        List<CommentVO> voList = commentPage.getRecords().stream()
                .map(comment -> {
                    CommentVO vo = new CommentVO();
                    BeanUtils.copyProperties(comment, vo);

                    // 设置用户信息
                    if (userMap != null) {
                        User user = userMap.get(comment.getUserId());
                        if (user != null) {
                            vo.setUserName(user.getUsername());
                            vo.setUserAvatar(user.getAvatar());
                        }
                    }
                    // 设置子评论数量
                    if(childCountMap!=null){
                        Map<String, Long> stringLongMap = childCountMap.get(comment.getId());
                        if(stringLongMap!=null){
                            vo.setChildCount(stringLongMap.getOrDefault("count",0L).intValue());
                        }

//                        vo.setChildCount(childCountMap.getOrDefault(comment.getId(), 0L).intValue());
                    }
                    return vo;
                })
                .collect(Collectors.toList());
        
        // 构建返回结果
        Page<CommentVO> resultPage = new Page<>();
        BeanUtils.copyProperties(commentPage, resultPage, "records");
        resultPage.setRecords(voList);
        
        return resultPage;
    }

    @Override
    @Transactional
    public void deleteComment(Long id) {
        // 检查评论是否存在
        Comment comment = this.getById(id);
        if (comment == null) {
            throw new BusinessException(ResultCode.COMMENT_NOT_FOUND);
        }
        
        // 检查是否是评论作者
        Long currentUserId = AuthUtils.getCurrentUserId();
        LoginUser currentUser = AuthUtils.getCurrentUser();
        if (!currentUser.getRole().equals("ADMIN")&&!comment.getUserId().equals(currentUserId)) {
            throw new BusinessException(ResultCode.NO_PERMISSION);
        }
        
        // 更新子评论的父评论ID为当前评论的父评论ID
        commentMapper.updateChildComments(comment.getId(),comment.getParentId(),new Date());
        // 删除当前评论（逻辑删除）
        boolean success = this.removeById(id);
        if (!success) {
            throw new BusinessException(ResultCode.INTERNAL_ERROR, "删除评论失败");
        }

        // 更新文章评论数
        articleService.updateCommentCount(comment.getArticleId(), false);
    }

    @Override
    public void changeTopStatus(Long id) {
        Comment comment = this.getById(id);
        if (comment == null) {
            throw new BusinessException(ResultCode.COMMENT_NOT_FOUND);
        }
        if(comment.getIsTop()==0){
            comment.setIsTop(1);
        }else{
            comment.setIsTop(0);
        }
        updateById(comment);
    }
}




