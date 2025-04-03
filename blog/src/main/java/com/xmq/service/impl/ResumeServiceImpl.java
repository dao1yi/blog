package com.xmq.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.xmq.model.entity.Resume;
import com.xmq.service.ResumeService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
@RequiredArgsConstructor
public class ResumeServiceImpl implements ResumeService {

    private final StringRedisTemplate stringRedisTemplate;
    private final ObjectMapper objectMapper;
    private static final String RESUME_KEY = "resume";  // 使用固定key

    @Override
    public void saveResume(Resume resume) {
        try {
            // 将 Resume 对象序列化为 JSON 字符串
            String resumeJson = objectMapper.writeValueAsString(resume);
            // 将 JSON 字符串存储到 Redis 中
            stringRedisTemplate.opsForValue().set(RESUME_KEY, resumeJson);
        } catch (IOException e) {
            // 处理序列化异常
            throw new RuntimeException("Failed to serialize resume to JSON", e);
        }
    }

    @Override
    public Resume getResume() {
        // 从 Redis 中获取 JSON 字符串
        String resumeJson = stringRedisTemplate.opsForValue().get(RESUME_KEY);
        if (resumeJson != null) {
            try {
                // 将 JSON 字符串反序列化为 Resume 对象
                return objectMapper.readValue(resumeJson, Resume.class);
            } catch (IOException e) {
                // 处理反序列化异常
                throw new RuntimeException("Failed to deserialize resume from JSON", e);
            }
        }
        return null;
    }

    @Override
    public void deleteResume() {
        // 从 Redis 中删除指定键的数据
        stringRedisTemplate.delete(RESUME_KEY);
    }

    @Override
    public void updateResume(Resume resume) {
        // 直接调用 saveResume 方法覆盖原有数据
        saveResume(resume);
    }
}