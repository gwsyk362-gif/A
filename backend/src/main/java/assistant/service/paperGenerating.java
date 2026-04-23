package assistant.service;

import assistant.entity.Question;
import assistant.mapper.QuestionMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class paperGenerating {

    @Autowired
    private QuestionMapper quesMapper;

    public List<Question> generateRandomPaper(Integer subId, Integer count) {
        LambdaQueryWrapper<Question> wrapper = new LambdaQueryWrapper<>();

        wrapper.eq(Question::getQuesSubId, subId);

        List<Question> allQuestions = quesMapper.selectList(wrapper);
        System.out.println("Found " + allQuestions.size() + " questions for subject " + subId);

        if (allQuestions == null || allQuestions.isEmpty()) {
            return new ArrayList<>();
        }

        Collections.shuffle(allQuestions);

        return allQuestions.stream()
                .limit(count)
                .collect(Collectors.toList());
    }
}