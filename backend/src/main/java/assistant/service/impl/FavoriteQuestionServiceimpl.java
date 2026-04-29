package assistant.service.impl;

import assistant.entity.FavoriteQuestion;
import assistant.entity.Question;
import assistant.mapper.FavoriteQuestionMapper;
import assistant.service.FavoriteQuestionService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FavoriteQuestionServiceimpl extends ServiceImpl<FavoriteQuestionMapper, FavoriteQuestion> implements FavoriteQuestionService {

    @Autowired
    private FavoriteQuestionMapper favoriteQuestionMapper;

    @Override
    public List<Question> getFavoriteQuestionDetails(Integer userId) {
        return favoriteQuestionMapper.getFavoriteQuestionDetails(userId);
    }
}