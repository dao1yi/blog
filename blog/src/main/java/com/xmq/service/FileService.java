package com.xmq.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.xmq.entity.FileUpload;
import org.springframework.web.multipart.MultipartFile;

public interface FileService extends IService<FileUpload> {
    
    /**
     * 上传文件
     * @param file 文件
     * @return 文件访问URL
     */
    String uploadFile(MultipartFile file);
    
    /**
     * 删除文件
     * @param id 文件ID
     */
    void deleteFile(Long id);
    
    /**
     * 获取文件列表
     * @param page 页码
     * @param size 每页记录数
     * @param fileName 文件名
     * @param fileType 文件类型
     * @return 文件列表
     */
    Page<FileUpload> getFileList(Integer page, Integer size, String fileName, String fileType);
} 