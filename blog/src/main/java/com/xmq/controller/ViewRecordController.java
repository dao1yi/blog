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

@RestController
@RequestMapping("/api/view-records")
@RequiredArgsConstructor
public class ViewRecordController {

    private final ViewRecordService viewRecordService;



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