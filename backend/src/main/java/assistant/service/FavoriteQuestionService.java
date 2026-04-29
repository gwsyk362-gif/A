package assistant.service;

import assistant.entity.FavoriteQuestion;
import assistant.entity.Question;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * 题目收藏 Service 接口
 */
public interface FavoriteQuestionService extends IService<FavoriteQuestion> {

    List<Question> getFavoriteQuestionDetails(Integer userId);
}