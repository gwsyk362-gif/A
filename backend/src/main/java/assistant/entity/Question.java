package assistant.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.math.BigDecimal;

@Data
@TableName("questions")
public class Question {
    @TableId(value = "ques_id", type = IdType.AUTO)
    private Integer quesId;

    private Integer quesSubId;

    private String quesContent;

    private String quesOptions;

    private String quesAnswer;

    private String quesAnalysis;

    private String quesKp;
    
}