package com.xmq.model.vo;

import lombok.Data;
import java.util.List;

@Data
public class ViewStatisticsVO {
    /**
     * 统计日期列表
     */
    private List<String> dateList;
    
    /**
     * 对应日期的浏览量列表
     */
    private List<Integer> valueList;
    
    /**
     * 总浏览量
     */
    private Integer totalViews;
    
    /**
     * 独立访客数
     */
    private Integer uniqueVisitors;
} 