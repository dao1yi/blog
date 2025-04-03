package com.xmq.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.xmq.annotation.RequireAdmin;
import com.xmq.common.Result;
import com.xmq.common.ResultCode;
import com.xmq.enums.StatisticsType;
import com.xmq.exception.BusinessException;
import com.xmq.model.vo.ViewStatisticsVO;
import com.xmq.service.ViewRecordService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/view-records")
@RequiredArgsConstructor
@Tag(name = "浏览记录管理", description = "文章浏览统计相关接口")
public class ViewRecordController {

    private final ViewRecordService viewRecordService;

    @Operation(summary = "获取浏览统计", description = "获取文章浏览量统计数据，支持不同时间维度的统计")
    @GetMapping("/statistics")
    @RequireAdmin
    public Result<ViewStatisticsVO> getViewStatistics(@RequestParam String type) throws JsonProcessingException {
        // 参数校验
        StatisticsType statisticsType = StatisticsType.fromCode(type);
        if (statisticsType == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR);
        }

        return Result.success(viewRecordService.getViewStatistics(statisticsType));
    }
} 