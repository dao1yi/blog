package com.xmq.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.xmq.common.ResultCode;
import com.xmq.constant.RedisConstants;
import com.xmq.entity.Article;
import com.xmq.entity.ViewRecord;
import com.xmq.exception.BusinessException;
import com.xmq.mapper.ViewRecordMapper;
import com.xmq.service.ArticleService;
import com.xmq.service.ViewRecordService;
import com.xmq.util.AuthUtils;
import com.xmq.enums.StatisticsType;
import com.xmq.model.vo.ViewStatisticsVO;
import jakarta.annotation.Resource;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.Calendar;

/**
* @author xumengqi
* @description 针对表【view_record(文章浏览记录表)】的数据库操作Service实现
* @createDate 2025-02-25 22:22:23
*/
@Service
public class ViewRecordServiceImpl extends ServiceImpl<ViewRecordMapper, ViewRecord>
    implements ViewRecordService {

    private final ArticleService articleService;

    public ViewRecordServiceImpl(ArticleService articleService) {
        this.articleService = articleService;
    }


    @Resource
    private ObjectMapper objectMapper;
    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Override
    @Transactional
    public void recordView(Long articleId, String ipAddress) {
        // 检查文章是否存在
        Article article = articleService.getById(articleId);
        if (article == null) {
            throw new BusinessException(ResultCode.ARTICLE_NOT_FOUND);
        }

        // 获取当前用户ID（如果已登录）
        final Long currentUserId = getCurrentUserId();

        // 检查30分钟内是否已经浏览过
        Date thirtyMinutesAgo = new Date(System.currentTimeMillis() - 30 * 60 * 1000);
        LambdaQueryWrapper<ViewRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ViewRecord::getArticleId, articleId)
               .ge(ViewRecord::getViewTime, thirtyMinutesAgo)
               .and(w -> w.eq(currentUserId != null, ViewRecord::getUserId, currentUserId)
                         .or()
                         .eq(currentUserId == null, ViewRecord::getIpAddress, ipAddress));

        // 如果30分钟内没有浏览记录，则创建新记录并更新浏览量
        if (this.count(wrapper) == 0) {
            // 创建浏览记录
            ViewRecord viewRecord = new ViewRecord();
            viewRecord.setArticleId(articleId);
            viewRecord.setUserId(currentUserId);
            viewRecord.setIpAddress(ipAddress);
            viewRecord.setViewTime(new Date());
            
            boolean success = this.save(viewRecord);
            if (!success) {
                throw new BusinessException(ResultCode.INTERNAL_ERROR, "记录浏览失败");
            }

            // 更新文章浏览量
            articleService.incrementViewCount(articleId);
        }
    }

    /**
     * 获取当前用户ID，未登录返回null
     */
    private Long getCurrentUserId() {
        try {
            return AuthUtils.getCurrentUserId();
        } catch (Exception ignored) {
            return null;
        }
    }

    /**
     * 获取今天的开始时间（00:00:00）
     */
    private Date getTodayStart() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTime();
    }

    /**
     * 获取指定天数前的开始时间（00:00:00）
     */
    private Date getDaysAgoStart(int days) {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_MONTH, -days);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTime();
    }

    /**
     * 获取指定月数前的开始时间（1号 00:00:00）
     */
    private Date getMonthsAgoStart(int months) {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MONTH, -months);
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTime();
    }

    @Override
    public ViewStatisticsVO getViewStatistics(StatisticsType type) throws JsonProcessingException {
        String s = stringRedisTemplate.opsForValue().get(RedisConstants.VIEW_STATISTICS_PREFIX + type);
        if(s!=null) {
            ViewStatisticsVO viewStatisticsVO = objectMapper.readValue(s, ViewStatisticsVO.class);
            if(viewStatisticsVO!=null){
                return viewStatisticsVO;
            }
        }
        // 查询统计数据
        List<Map<String, Object>> statistics;
        switch (type) {
            case DAY:
                // 今天的统计数据（从今天00:00:00开始）
                statistics = this.baseMapper.statisticsByDay(getTodayStart());
                break;
            case WEEK:
                // 最近7天的统计数据（从7天前的00:00:00开始）
                statistics = this.baseMapper.statisticsByWeek(getDaysAgoStart(6));
                break;
            case MONTH:
                // 最近30天的统计数据（从30天前的00:00:00开始）
                statistics = this.baseMapper.statisticsByMonth(getDaysAgoStart(29));
                break;
            case YEAR:
                // 最近12个月的统计数据（从12个月前的1号00:00:00开始）
                statistics = this.baseMapper.statisticsByYear(getMonthsAgoStart(11));
                break;
            default:
                throw new IllegalArgumentException("Unsupported statistics type");
        }

        // 构建返回结果
        ViewStatisticsVO result = new ViewStatisticsVO();
        
        // 设置日期列表
        List<String> dateList = statistics.stream()
            .map(map -> (String) map.get("dateLabel"))
            .collect(Collectors.toList());
        result.setDateList(dateList);

        // 设置浏览量列表
        List<Integer> valueList = statistics.stream()
            .map(map -> ((Number) map.get("totalViews")).intValue())
            .collect(Collectors.toList());
        result.setValueList(valueList);

        // 计算总浏览量
        int totalViews = valueList.stream().mapToInt(Integer::intValue).sum();
        result.setTotalViews(totalViews);

        // 计算总独立访客数（仅在按天统计时有意义）
        int uniqueVisitors = type == StatisticsType.DAY ? 
            statistics.stream()
                .mapToInt(map -> ((Number) map.get("uniqueVisitors")).intValue())
                .sum() :
            0;
        result.setUniqueVisitors(uniqueVisitors);
        String resultString = objectMapper.writeValueAsString(result);

        switch (type) {
            case DAY:
                // 今天的统计数据（从今天00:00:00开始）
                stringRedisTemplate.opsForValue().set(RedisConstants.VIEW_STATISTICS_PREFIX+type,resultString,10L, TimeUnit.MINUTES);
                break;
            case WEEK:
                // 最近7天的统计数据（从7天前的00:00:00开始）
                stringRedisTemplate.opsForValue().set(RedisConstants.VIEW_STATISTICS_PREFIX+type,resultString,3L, TimeUnit.HOURS);
                break;
            case MONTH:
                // 最近30天的统计数据（从30天前的00:00:00开始）
                stringRedisTemplate.opsForValue().set(RedisConstants.VIEW_STATISTICS_PREFIX+type,resultString,12L, TimeUnit.HOURS);
                break;
            case YEAR:
                // 最近12个月的统计数据（从12个月前的1号00:00:00开始）
                stringRedisTemplate.opsForValue().set(RedisConstants.VIEW_STATISTICS_PREFIX+type,resultString,1L, TimeUnit.DAYS);
                break;
            default:
                throw new IllegalArgumentException("Unsupported statistics type");
        }
        return result;
    }
}




