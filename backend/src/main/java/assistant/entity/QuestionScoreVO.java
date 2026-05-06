package assistant.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class QuestionScoreVO {
    private Integer quesId;      // 题目ID
    private Double finalScore;   // 最终推荐得分
}