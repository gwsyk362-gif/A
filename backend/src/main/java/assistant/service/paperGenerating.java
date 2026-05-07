package assistant.service;

import assistant.entity.Question;
import assistant.entity.UserSubjectScore;
import assistant.mapper.QuestionMapper;
import assistant.mapper.UserSubjectScoreMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
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

    @Autowired
    private UserSubjectScoreMapper userSubjectScoreMapper;

    public List<Question> generatePersonalizedPaper(Integer userId, Integer subId, Integer count) {

        LambdaQueryWrapper<Question> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Question::getQuesSubId, subId);
        List<Question> allQuestions = quesMapper.selectList(wrapper);

        if (allQuestions == null || allQuestions.isEmpty()) {
            return new ArrayList<>();
        }

        UserSubjectScore uss = userSubjectScoreMapper.selectOne(
                new QueryWrapper<UserSubjectScore>().eq("user_id", userId).eq("sub_id", subId)
        );
        double userAbility = (uss != null && uss.getEloScore() != null) ? uss.getEloScore() : 1500.0;
        // ========================================================

        // 纯 ZPD 能力匹配
        int poolSize = count * 3;
        List<Question> candidateQuestions = allQuestions.stream()
                .sorted((q1, q2) -> {
                    double score1 = (q1.getQuesScore() != null) ? q1.getQuesScore() : 1500.0;
                    double score2 = (q2.getQuesScore() != null) ? q2.getQuesScore() : 1500.0;
                    return Double.compare(Math.abs(score1 - userAbility), Math.abs(score2 - userAbility));
                })
                .limit(poolSize)
                .collect(Collectors.toList());

        Collections.shuffle(candidateQuestions);

        return candidateQuestions.stream()
                .limit(count)
                .collect(Collectors.toList());
    }
}