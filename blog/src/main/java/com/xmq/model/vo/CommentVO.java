package com.xmq.model.vo;

import lombok.Data;
import java.util.Date;

@Data
public class CommentVO {
    
    private Long id;            // 评论ID
    private String content;     // 评论内容
    private Long userId;        // 评论用户ID
    private String userName;    // 评论用户名
    private String userAvatar;  // 评论用户头像
    private Long parentId;      // 父评论ID
    private Integer childCount; // 子评论数量
    private Date createdTime;   // 评论时间
} 