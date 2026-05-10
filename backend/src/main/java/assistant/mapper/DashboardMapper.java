package assistant.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

@Mapper
public interface DashboardMapper {

    //查题目总数
    @Select("SELECT COUNT(*) FROM questions")
    long getTotalQuestions();

    // 查视频总数
    @Select("SELECT COUNT(*) FROM video_resources")
    long getTotalVideos();

    // 今日有效日活
    @Select("SELECT COUNT(DISTINCT user_id) FROM login_logs WHERE login_date = CURDATE()")
    long getDailyActiveUsers();

    // 每个科目的题目数量
    @Select("SELECT s.sub_name AS name, COUNT(q.ques_id) AS value " +
            "FROM subjects s " +
            "LEFT JOIN questions q ON s.sub_id = q.ques_sub_id " +
            "GROUP BY s.sub_id " +
            "HAVING value > 0")
    List<Map<String, Object>> getSubjectQuestionDistribution();

    //每个科目的视频数量
    @Select("SELECT s.sub_name AS name, COUNT(v.vid_id) AS value " +
            "FROM subjects s " +
            "LEFT JOIN video_resources v ON s.sub_id = v.vid_sub_id " +
            "GROUP BY s.sub_id " +
            "HAVING value > 0")
    List<Map<String, Object>> getSubjectVideoDistribution();

    // 近7天平均正确率
    @Select("SELECT DATE_FORMAT(rec_time, '%m-%d') AS dateStr, " +
            "ROUND((SUM(rec_is_correct) / COUNT(*)) * 100, 1) AS correctRate " +
            "FROM practice_records " +
            "WHERE rec_time >= DATE_SUB(CURDATE(), INTERVAL 7 DAY) " +
            "GROUP BY DATE_FORMAT(rec_time, '%m-%d') " +
            "ORDER BY dateStr ASC")
    List<Map<String, Object>> getSevenDaysAccuracy();

    // 近7天日活趋势
    @Select("SELECT DATE_FORMAT(ll.login_date, '%m-%d') AS dateStr, " +
            "COUNT(DISTINCT ll.user_id) AS activeCount " +
            "FROM login_logs ll " +
            "JOIN users u ON ll.user_id = u.user_id " +
            "WHERE ll.login_date >= DATE_SUB(CURDATE(), INTERVAL 7 DAY) " +
            "AND u.role != 'admin' " +
            "GROUP BY ll.login_date " +
            "ORDER BY ll.login_date ASC")
    List<Map<String, Object>> getDailyActiveTrend();

    // 近30天平均正确率
    @Select("SELECT DATE_FORMAT(rec_time, '%m-%d') AS dateStr, " +
            "ROUND((SUM(rec_is_correct) / COUNT(*)) * 100, 1) AS correctRate " +
            "FROM practice_records " +
            "WHERE rec_time >= DATE_SUB(CURDATE(), INTERVAL 30 DAY) " +
            "GROUP BY DATE_FORMAT(rec_time, '%m-%d') " +
            "ORDER BY dateStr ASC")
    List<Map<String, Object>> getThirtyDaysAccuracy();

    // 近30天日活趋势
    @Select("SELECT DATE_FORMAT(ll.login_date, '%m-%d') AS dateStr, " +
            "COUNT(DISTINCT ll.user_id) AS activeCount " +
            "FROM login_logs ll " +
            "JOIN users u ON ll.user_id = u.user_id " +
            "WHERE ll.login_date >= DATE_SUB(CURDATE(), INTERVAL 30 DAY) " +
            "AND u.role != 'admin' " +
            "GROUP BY ll.login_date " +
            "ORDER BY ll.login_date ASC")
    List<Map<String, Object>> getThirtyDaysActive();

}