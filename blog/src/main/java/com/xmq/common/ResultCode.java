package com.xmq.common;

import lombok.Getter;

@Getter
public enum ResultCode {
    
    SUCCESS(200, "操作成功"),
    UNAUTHORIZED(401, "请登录后操作"),
    FORBIDDEN(403, "没有操作权限"),
    NOT_FOUND(404, "请求的资源不存在"),
    INTERNAL_ERROR(500, "服务器内部错误"),
    PARAM_ERROR(400, "参数错误"),
    
    // 用户相关错误码
    USER_NOT_FOUND(1001, "用户不存在"),
    USERNAME_EXISTED(1002, "用户名已存在"),
    EMAIL_EXISTED(1003, "邮箱已存在"),
    PASSWORD_ERROR(1004, "密码错误"),
    
    // 文章相关错误码
    ARTICLE_NOT_FOUND(2001, "文章不存在"),
    NO_PERMISSION(2002, "没有操作权限"),
    ARTICLE_CREATE_ERROR(2003, "创建文章失败"),
    ARTICLE_UPDATE_ERROR(2004, "更新文章失败"),
    ARTICLE_DELETE_ERROR(2005, "删除文章失败"),
    ARTICLE_TITLE_EMPTY(2006, "文章标题不能为空"),
    ARTICLE_CONTENT_EMPTY(2007, "文章内容不能为空"),
    
    // 评论相关错误码
    COMMENT_NOT_FOUND(3001, "评论不存在"),
    
    // 文件相关错误码
    FILE_UPLOAD_ERROR(4001, "文件上传失败"),
    FILE_NOT_FOUND(4002, "文件不存在"),
    FILE_SIZE_EXCEED(4003, "文件大小超过限制"),
    FILE_TYPE_NOT_ALLOWED(4004, "不支持的文件类型"),
    FILE_DELETE_ERROR(4005, "文件删除失败");
    
    private final int code;
    private final String message;
    
    ResultCode(int code, String message) {
        this.code = code;
        this.message = message;
    }
} 