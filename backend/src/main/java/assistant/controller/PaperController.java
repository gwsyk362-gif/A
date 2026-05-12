package assistant.controller;

import assistant.entity.Question;
import assistant.mapper.QuestionMapper;
import assistant.service.UserBasedRecommendService;
import assistant.service.paperGenerating;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;


@CrossOrigin
@RestController
public class PaperController {

    @Autowired
    private paperGenerating paperGenerating;

    @Autowired
    private UserBasedRecommendService userBasedRecommendService;

    @Autowired
    private QuestionMapper questionMapper;

    @GetMapping("/generatePersonalized")
    public List<Question> getPersonalizedPaper(
            @RequestParam Integer userId,
            @RequestParam Integer subId,
            @RequestParam Integer count) {

        System.out.println("Generating personalized paper for userId: " + userId + ", subId: " + subId + ", count: " + count);

        List<Question> result = paperGenerating.generatePersonalizedPaper(userId, subId, count);

        System.out.println("Generated " + result.size() + " personalized questions for user " + userId);
        return result;
    }

    @GetMapping("/getQuestions")
    public List<Question> getRecommendQuestions(
            @RequestParam Integer userId,
            @RequestParam Integer subjectId,
            @RequestParam(required = false) String kp,
            @RequestParam(defaultValue = "5") int count,
            @RequestParam(required = false) String keyword) {

        return userBasedRecommendService.recommendQuestions(userId, count, subjectId, kp, keyword);
    }

    //获取知识点列表
    @GetMapping("/knowledgePoints")
    public List<String> getKnowledgePoints(@RequestParam Integer subId) {
        return questionMapper.selectDistinctKpBySubject(subId);
    }

    // 按科目和知识点获取所有题目
    @GetMapping("/practice/all")
    public List<Question> getAllPracticeQuestions(@RequestParam Integer subjectId,
                                                  @RequestParam(required = false) String kp) {
        com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Question> wrapper = new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<>();

        wrapper.eq(Question::getQuesSubId, subjectId);

        if (kp != null && !kp.trim().isEmpty()) {
            wrapper.eq(Question::getQuesKp, kp);
        }

        return questionMapper.selectList(wrapper);
    }
}