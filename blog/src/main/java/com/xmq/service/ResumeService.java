package com.xmq.service;

import com.xmq.model.entity.Resume;

public interface ResumeService {
    /**
     * 保存简历信息到Redis
     * @param resume 简历信息
     */
    void saveResume(Resume resume);

    /**
     * 从Redis获取简历信息
     * @return 简历信息
     */
    Resume getResume();

    /**
     * 从Redis删除简历信息
     */
    void deleteResume();

    /**
     * 更新简历信息
     * @param resume 简历信息
     */
    void updateResume(Resume resume);
} 