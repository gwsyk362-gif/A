package assistant.mapper;

import assistant.entity.PracticeRecord;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface PracticeRecordMapper extends BaseMapper<PracticeRecord> {

    @Select("SELECT r.rec_ques_id " +
            "FROM practice_records r " +
            "JOIN questions q ON r.rec_ques_id = q.ques_id " +
            "WHERE q.ques_sub_id = #{subjectId} " +
            "GROUP BY r.rec_ques_id " +
            "ORDER BY COUNT(r.rec_id) DESC " +
            "LIMIT #{limit}")
    List<Integer> selectHotQuestionIds(@Param("subjectId") Integer subjectId, @Param("limit") int limit);
}
