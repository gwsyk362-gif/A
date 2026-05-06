package assistant.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("user_subject_scores")
public class UserSubjectScore {

    @TableId(value = "score_id", type = IdType.AUTO)
    private Integer scoreId;

    private Integer userId;

    private Integer subId;

    private Integer eloScore;

    private LocalDateTime updateTime;
}