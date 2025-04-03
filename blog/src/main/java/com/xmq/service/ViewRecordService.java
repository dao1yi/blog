package com.xmq.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.xmq.entity.ViewRecord;
import com.xmq.enums.StatisticsType;
import com.xmq.model.vo.ViewStatisticsVO;

/**
* @author xumengqi
* @description 针对表【view_record(文章浏览记录表)】的数据库操作Service
* @createDate 2025-02-25 22:22:23
*/
public interface ViewRecordService extends IService<ViewRecord> {
    
    /**
     * 记录文章浏览
     * @param articleId 文章ID
     * @param ipAddress 访问者IP地址
     */
    void recordView(Long articleId, String ipAddress);

    /**
     * 获取浏览统计数据
     * @param type 统计类型（按天/按周/按月/按年）
     * @return 统计结果：
     *         - 按天：今天的总访问量和独立访客量
     *         - 按周：最近7天每天的访问量
     *         - 按月：最近一个月每天的访问量
     *         - 按年：最近一年12个月的每月访问量
     */
    ViewStatisticsVO getViewStatistics(StatisticsType type) throws JsonProcessingException;
}
