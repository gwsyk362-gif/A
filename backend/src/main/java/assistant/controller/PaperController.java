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
                                             @RequestParam Integer subjectId) {
        // 获取推荐的题目 ID 列表
        List<Integer> quesIds = userBasedRecommendService.recommendQuestions(userId, count, subjectId);

        // 将 ID 列表转换为完整的题目对象列表
        if (quesIds.isEmpty()) return new ArrayList<>();

        // 使用 selectBatchIds 直接查询完整信息
        return questionMapper.selectBatchIds(quesIds);
    }
}