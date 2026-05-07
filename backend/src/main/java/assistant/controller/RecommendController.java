package assistant.controller;

import assistant.entity.Question;
import assistant.service.UserBasedRecommendService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/recommend")
public class RecommendController {

    @Autowired
    private UserBasedRecommendService recommendService;

    @GetMapping("/getQuestions")
    public ResponseEntity<?> getRecommendedQuestions(
            @RequestParam Integer userId,
            @RequestParam Integer subjectId,
            @RequestParam(defaultValue = "5") Integer count,
            @RequestParam(required = false) String kp) {

        try {
            List<Question> recommendedQuestions = recommendService.recommendQuestions(userId, count, subjectId, kp);
            return ResponseEntity.ok(recommendedQuestions);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("推荐系统异常: " + e.getMessage());
        }
    }
}