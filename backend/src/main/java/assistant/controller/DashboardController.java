package assistant.controller;

import assistant.service.UserService;
import assistant.entity.User;
import assistant.mapper.DashboardMapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;

@RestController
@RequestMapping("/api/admin/dashboard")
@CrossOrigin
public class DashboardController {

    @Autowired
    private UserService userService;

    @Autowired
    private DashboardMapper dashboardMapper;

    @GetMapping
    public Map<String, Object> getDashboardData() {
        Map<String, Object> result = new HashMap<>();

        // 1. 基础总数统计
        long totalUsers = userService.count(new QueryWrapper<User>().ne("role", "admin"));
        long totalQuestions = dashboardMapper.getTotalQuestions();
        long totalVideos = dashboardMapper.getTotalVideos();
        long dailyActive = dashboardMapper.getDailyActiveUsers();

        result.put("totalUsers", totalUsers);
        result.put("dailyActive", dailyActive);
        result.put("totalQuestions", totalQuestions);
        result.put("totalVideos", totalVideos);

        // 2. 饼图数据：各科目题库分布
        List<Map<String, Object>> subjectResources = dashboardMapper.getSubjectQuestionDistribution();
        result.put("subjectResources", subjectResources);
        // 视频分布
        List<Map<String, Object>> subjectVideos = dashboardMapper.getSubjectVideoDistribution();
        result.put("subjectVideos", subjectVideos);

        // 近7天平均正确率趋势
        List<Map<String, Object>> accuracyRaw = dashboardMapper.getSevenDaysAccuracy();
        List<String> accuracyDates = new ArrayList<>();
        List<Double> correctRates = new ArrayList<>();
        for (Map<String, Object> row : accuracyRaw) {
            accuracyDates.add(row.get("dateStr").toString());
            Object rateObj = row.get("correctRate");
            correctRates.add(rateObj != null ? Double.parseDouble(rateObj.toString()) : 0.0);
        }
        Map<String, Object> accuracyTrend = new HashMap<>();
        accuracyTrend.put("dates", accuracyDates);
        accuracyTrend.put("correctRates", correctRates);
        result.put("accuracyTrend", accuracyTrend);

        //近7天日活趋势
        List<Map<String, Object>> activeRaw = dashboardMapper.getDailyActiveTrend();
        List<String> activeDates = new ArrayList<>();
        List<Integer> activeUsers = new ArrayList<>();
        for (Map<String, Object> row : activeRaw) {
            activeDates.add(row.get("dateStr").toString());
            activeUsers.add(Integer.parseInt(row.get("activeCount").toString()));
        }
        Map<String, Object> activeTrend = new HashMap<>();
        activeTrend.put("dates", activeDates);
        activeTrend.put("activeUsers", activeUsers);
        result.put("activeTrend", activeTrend);

        return result;
    }

    //近30天正确率
    @GetMapping("/monthly-accuracy")
    public Map<String, Object> getMonthlyAccuracy() {
        List<Map<String, Object>> raw = dashboardMapper.getThirtyDaysAccuracy();
        List<String> dates = new ArrayList<>();
        List<Double> rates = new ArrayList<>();
        for (Map<String, Object> row : raw) {
            dates.add(row.get("dateStr").toString());
            Object rateObj = row.get("correctRate");
            rates.add(rateObj != null ? Double.parseDouble(rateObj.toString()) : 0.0);
        }
        Map<String, Object> result = new HashMap<>();
        result.put("dates", dates);
        result.put("correctRates", rates);
        return result;
    }

    //近30天日活
    @GetMapping("/monthly-active")
    public Map<String, Object> getMonthlyActive() {
        List<Map<String, Object>> raw = dashboardMapper.getThirtyDaysActive();
        List<String> dates = new ArrayList<>();
        List<Integer> users = new ArrayList<>();
        for (Map<String, Object> row : raw) {
            dates.add(row.get("dateStr").toString());
            users.add(Integer.parseInt(row.get("activeCount").toString()));
        }
        Map<String, Object> result = new HashMap<>();
        result.put("dates", dates);
        result.put("activeUsers", users);
        return result;
    }
}