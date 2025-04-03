package com.xmq.model.entity;

import lombok.Data;
import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * 个人简历实体类
 */
@Data
public class Resume implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;

    // 基本信息
    private String name;            // 姓名
    private String avatar;          // 照片
    private String email;           // 邮箱
    private String introduction;    // 个人简介
    
    // 职业相关
    private String status; //   求职状态
    private String jobIntention;    // 求职意向
    private List<String> skills;    // 技能列表
    private List<String> honors;    // 荣誉成就列表
    private String resumeUrl;       // 简历链接
    private String projectUrl;      // 项目链接

    // 教育背景
    private String schoolName;      // 就读院校
    private String major;           // 专业
    private String degree;          // 学历
    private Date startDate;         // 入学时间
    private Date graduateDate;      // 毕业时间

    // 时间信息
    private Date createTime;        // 创建时间
    private Date updateTime;        // 更新时间
} 