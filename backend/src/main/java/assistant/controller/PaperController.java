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

    @GetMapping("/generate")
    public List<Question> getPaper(@RequestParam Integer subId, @RequestParam Integer count) {
        System.out.println("Generating paper for subId: " + subId + ", count: " + count);
        List<Question> result = paperGenerating.generateRandomPaper(subId, count);
        System.out.println("Generated " + result.size() + " questions");
        return result;
    }

    @GetMapping("/recommend")
    public List<Question> recommendQuestions(@RequestParam Integer userId,
                                             @RequestParam int count,
                                             @RequestParam Integer subjectId,
                                             @RequestParam(required = false) String kp) {
        List<Integer> quesIds = userBasedRecommendService.recommendQuestions(userId, count, subjectId, kp);

        // 将 ID 列表转换为完整的题目列表
        if (quesIds.isEmpty()) return new ArrayList<>();

        // 用selectBatchIds查询完整信息
        return questionMapper.selectBatchIds(quesIds);
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