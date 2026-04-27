package assistant.mapper;

import assistant.entity.FavoriteQuestion;
import assistant.entity.Question;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface FavoriteQuestionMapper extends BaseMapper<FavoriteQuestion> {

    @Select("SELECT q.* FROM questions q " +
            "JOIN favorites_questions f ON q.ques_id = f.fav_ques_id " +
            "WHERE f.fav_user_id = #{userId}")
    List<Question> getFavoriteQuestionDetails(@Param("userId") Integer userId);
}