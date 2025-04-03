package com.xmq.util;

import org.springframework.web.multipart.MultipartFile;

public class FileUtils {
    
    /**
     * 获取文件扩展名
     */
    public static String getExtension(String fileName) {
        if (fileName == null) {
            return null;
        }
        int index = fileName.lastIndexOf(".");
        if (index == -1) {
            return "";
        }
        return fileName.substring(index + 1);
    }
    
    /**
     * 获取文件扩展名
     */
    public static String getExtension(MultipartFile file) {
        return getExtension(file.getOriginalFilename());
    }
    
    /**
     * 生成新的文件名
     */
    public static String generateFileName(String originalFilename) {
        String extension = getExtension(originalFilename);
        return StringUtils.generateUUID() + (StringUtils.isEmpty(extension) ? "" : "." + extension);
    }
    
    /**
     * 判断是否为图片
     */
    public static boolean isImage(String fileName) {
        String extension = getExtension(fileName).toLowerCase();
        return "jpg".equals(extension) || 
               "jpeg".equals(extension) || 
               "png".equals(extension) || 
               "gif".equals(extension);
    }
    
    /**
     * 判断是否为图片
     */
    public static boolean isImage(MultipartFile file) {
        return isImage(file.getOriginalFilename());
    }
} 