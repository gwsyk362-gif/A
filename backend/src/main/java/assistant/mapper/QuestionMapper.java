package assistant.mapper;

import assistant.entity.Question;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface QuestionMapper extends BaseMapper<Question> {

    @Select("SELECT ques_id FROM questions WHERE ques_sub_id = #{subjectId} ORDER BY RAND() LIMIT #{limit}")
    List<Integer> selectRandomIdsBySubject(@Param("subjectId") Integer subjectId, @Param("limit") int limit);

    @Select("SELECT DISTINCT ques_kp FROM questions WHERE ques_sub_id = #{subId} AND ques_kp IS NOT NULL AND ques_kp != ''")
    List<String> selectDistinctKpBySubject(@Param("subId") Integer subId);


}