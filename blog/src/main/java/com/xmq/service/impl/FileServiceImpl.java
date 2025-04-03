package com.xmq.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xmq.common.ResultCode;
import com.xmq.entity.FileUpload;
import com.xmq.exception.BusinessException;
import com.xmq.mapper.FileUploadMapper;
import com.xmq.service.FileService;
import com.xmq.util.AuthUtils;
import com.xmq.util.FileUtils;
import io.minio.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.util.StringUtils;
import java.util.Date;

@Slf4j
@Service
public class FileServiceImpl extends ServiceImpl<FileUploadMapper, FileUpload> implements FileService {
    
    @Autowired
    private MinioClient minioClient;

    @Value("${minio.bucketName}")
    private String bucketName;

    @Value("${minio.urlPrefix}")
    private String urlPrefix;

    private final FileUploadMapper fileUploadMapper;

    public FileServiceImpl(FileUploadMapper fileUploadMapper) {
        this.fileUploadMapper = fileUploadMapper;
    }

    @Override
    @Transactional
    public String uploadFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "文件不能为空");
        }
        
        try {
            // 生成文件名
            String fileName = FileUtils.generateFileName(file.getOriginalFilename());
            
            // 检查存储桶是否存在，不存在则创建
            boolean found = minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build());
            if (!found) {
                minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
            }
            
            // 上传文件
            minioClient.putObject(
                PutObjectArgs.builder()
                    .bucket(bucketName)
                    .object(fileName)
                    .stream(file.getInputStream(), file.getSize(), -1)
                    .contentType(file.getContentType())
                    .build()
            );
            
            // 生成访问URL
            String fileUrl = urlPrefix + "/" + fileName;
            
            // 保存文件记录到数据库
            FileUpload fileUpload = new FileUpload();
            fileUpload.setFileName(file.getOriginalFilename());
            fileUpload.setFileUrl(fileUrl);
            fileUpload.setFileSize(file.getSize());
            fileUpload.setFileType(file.getContentType());
            fileUpload.setUserId(AuthUtils.getCurrentUserId());
            fileUpload.setCreatedTime(new Date());
            fileUpload.setUpdatedTime(new Date());
            
            save(fileUpload);
            
            // 只返回文件URL
            return fileUrl;
            
        } catch (Exception e) {
            log.error("文件上传失败：", e);
            throw new BusinessException(ResultCode.FILE_UPLOAD_ERROR);
        }
    }

    @Override
    @Transactional
    public void deleteFile(Long id) {
        FileUpload fileUpload = getById(id);
        if (fileUpload == null) {
            throw new BusinessException(ResultCode.FILE_NOT_FOUND);
        }
        
        try {
            // 从MinIO删除文件
            String fileName = fileUpload.getFileUrl().substring(fileUpload.getFileUrl().lastIndexOf("/") + 1);
            minioClient.removeObject(
                RemoveObjectArgs.builder()
                    .bucket(bucketName)
                    .object(fileName)
                    .build()
            );
            
            // 删除数据库记录
            removeById(id);
            
        } catch (Exception e) {
            log.error("文件删除失败：", e);
            throw new BusinessException(ResultCode.FILE_DELETE_ERROR);
        }
    }

    @Override
    public Page<FileUpload> getFileList(Integer page, Integer size, String fileName, String fileType) {
        Page<FileUpload> pageParam = new Page<>(page, size);
        
        LambdaQueryWrapper<FileUpload> queryWrapper = new LambdaQueryWrapper<>();
        
        // 添加文件名模糊查询条件
        if (StringUtils.hasText(fileName)) {
            queryWrapper.like(FileUpload::getFileName, fileName);
        }
        
        // 添加文件类型精确匹配条件
        if (StringUtils.hasText(fileType)) {
            queryWrapper.likeLeft(FileUpload::getFileType, fileType);
        }
        
        // 按创建时间降序排序
        queryWrapper.orderByDesc(FileUpload::getCreatedTime);
        
        return fileUploadMapper.selectPage(pageParam, queryWrapper);
    }
} 