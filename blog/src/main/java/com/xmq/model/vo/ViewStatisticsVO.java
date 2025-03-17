package com.xmq.model.vo;

import lombok.Data;
import java.util.List;

@Data
public class ViewStatisticsVO {
    private List<String> dateList;
    private List<Integer> valueList;
    private Integer totalViews;
    private Integer uniqueVisitors;
} 