package assistant.controller;

import assistant.entity.Question;
import assistant.service.UserBasedRecommendService;
import assistant.service.paperGenerating;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;


@CrossOrigin
@RestController
public class PaperController {

    @Autowired
    private paperGenerating paperGenerating;

    @Autowired
    private UserBasedRecommendService userBasedRecommendService;

    @GetMapping("/generate")
    public List<Question> getPaper(@RequestParam Integer subId, @RequestParam Integer count) {
        System.out.println("Generating paper for subId: " + subId + ", count: " + count);
        List<Question> result = paperGenerating.generateRandomPaper(subId, count);
        System.out.println("Generated " + result.size() + " questions");
        return result;
    }

    @GetMapping("/recommend")
    public List<Integer> recommendQuestions(@RequestParam Integer userId, @RequestParam Integer count) {
        System.out.println("Recommending questions for userId: " + userId + ", count: " + count);
        List<Integer> result = userBasedRecommendService.recommendQuestions(userId, count);
        System.out.println("Recommended " + result.size() + " question IDs: " + result);
        return result;
    }
}