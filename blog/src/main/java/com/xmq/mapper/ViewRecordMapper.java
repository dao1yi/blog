package com.xmq.mapper;

import com.xmq.entity.ViewRecord;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
* @author xumengqi
* @description 针对表【view_record(文章浏览记录表)】的数据库操作Mapper
* @createDate 2025-02-25 22:22:23
* @Entity com.xmq.entity.ViewRecord
*/
public interface ViewRecordMapper extends BaseMapper<ViewRecord> {
    
    /**
     * 按天统计浏览数据
     */
    List<Map<String, Object>> statisticsByDay(@Param("startDate") Date startDate);

    /**
     * 按周统计浏览数据
     */
    List<Map<String, Object>> statisticsByWeek(@Param("startDate") Date startDate);

    /**
     * 按月统计浏览数据
     */
    List<Map<String, Object>> statisticsByMonth(@Param("startDate") Date startDate);

    /**
     * 按年统计浏览数据（按月份汇总）
     */
    List<Map<String, Object>> statisticsByYear(@Param("startDate") Date startDate);
}




