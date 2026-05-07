package assistant.mapper;

import assistant.entity.PracticeRecord;
import assistant.entity.SubjectCoverageDTO;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

@Mapper
public interface PracticeRecordMapper extends BaseMapper<PracticeRecord> {

    // 查询知识点掌握情况 (按科目分类，包含所有知识点)
    @Select("SELECT q.ques_sub_id AS subId, q.ques_kp AS kpName, " +
            "IFNULL(ROUND(AVG(r.rec_is_correct) * 100, 2), 0) AS correctRate " +
            "FROM questions q " +
            "LEFT JOIN practice_records r ON q.ques_id = r.rec_ques_id AND r.rec_user_id = #{userId} " +
            "WHERE q.ques_kp IS NOT NULL AND q.ques_kp != '' " +
            "GROUP BY q.ques_sub_id, q.ques_kp")
    List<Map<String, Object>> getKnowledgeMastery(@Param("userId") Integer userId);

    // 查询近7天每日做题数量与正确率
    @Select("SELECT DATE(rec_time) AS dateStr, COUNT(rec_id) AS totalCount, " +
            "ROUND(SUM(rec_is_correct) / COUNT(rec_id) * 100, 2) AS correctRate " +
            "FROM practice_records " +
            "WHERE rec_user_id = #{userId} AND rec_time >= DATE_SUB(CURDATE(), INTERVAL 7 DAY) " +
            "GROUP BY DATE(rec_time) ORDER BY dateStr ASC")
    List<Map<String, Object>> getDailyStats(@Param("userId") Integer userId);

    // 获取错题本详情 (关联题目表)
    @Select("SELECT q.ques_id as quesId, q.ques_content as quesContent, q.ques_options as quesOptions, " +
            "q.ques_answer as quesAnswer, q.ques_analysis as quesAnalysis, " +
            "r.rec_user_answer as recUserAnswer, r.rec_time as recTime " +
            "FROM practice_records r " +
            "JOIN questions q ON r.rec_ques_id = q.ques_id " +
            "WHERE r.rec_user_id = #{userId} AND r.rec_is_correct = 0 " +
            "ORDER BY r.rec_time DESC")
    List<Map<String, Object>> getErrorRecords(@Param("userId") Integer userId);

    // 查询当年的每日做题活跃度
    @Select("SELECT DATE(rec_time) AS dateStr, COUNT(rec_id) AS totalCount " +
            "FROM practice_records " +
            "WHERE rec_user_id = #{userId} AND YEAR(rec_time) = YEAR(CURDATE()) " +
            "GROUP BY DATE(rec_time) " +
            "ORDER BY dateStr ASC")
    List<Map<String, Object>> getYearlyStats(@Param("userId") Integer userId);

    // 查询各科目知识点覆盖率
    @Select("SELECT s.sub_id AS subId, s.sub_name AS subName, " +
            "COUNT(DISTINCT q.ques_kp) AS totalKp, " +
            "COUNT(DISTINCT CASE WHEN r.rec_user_id = #{userId} THEN q.ques_kp END) AS practicedKp " +
            "FROM subjects s " +
            "LEFT JOIN questions q ON s.sub_id = q.ques_sub_id AND q.ques_kp IS NOT NULL AND q.ques_kp != '' " +
            "LEFT JOIN practice_records r ON q.ques_id = r.rec_ques_id " +
            "GROUP BY s.sub_id, s.sub_name")
    List<SubjectCoverageDTO> getSubjectCoverage(@Param("userId") Integer userId);

}