package assistant.mapper;

import assistant.entity.Question;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface QuestionMapper extends BaseMapper<Question> {

    //根据科目随机找题
    @Select("SELECT ques_id FROM questions WHERE ques_sub_id = #{subjectId} ORDER BY RAND() LIMIT #{limit}")
    List<Integer> selectRandomIdsBySubject(@Param("subjectId") Integer subjectId, @Param("limit") int limit);

    //根据知识点找题
    @Select("SELECT DISTINCT ques_kp FROM questions WHERE ques_sub_id = #{subId} AND ques_kp IS NOT NULL AND ques_kp != ''")
    List<String> selectDistinctKpBySubject(@Param("subId") Integer subId);

    //按热度找题
    @Select("SELECT r.rec_ques_id " +
            "FROM practice_records r " +
            "JOIN questions q ON r.rec_ques_id = q.ques_id " +
            "WHERE q.ques_sub_id = #{subjectId} " +
            "GROUP BY r.rec_ques_id " +
            "ORDER BY COUNT(r.rec_id) DESC " +
            "LIMIT #{limit}")
    List<Integer> selectHotQuestionIds(@Param("subjectId") Integer subjectId, @Param("limit") int limit);
}