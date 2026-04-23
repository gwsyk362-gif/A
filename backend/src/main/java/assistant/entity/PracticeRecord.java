package assistant.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("practice_records")
public class PracticeRecord {
    @TableId(value = "rec_id", type = IdType.AUTO)
    private Long recId;

    private Integer recUserId;

    private Integer recQuesId;

    private String recUserAnswer;

    private Integer recIsCorrect;

    private LocalDateTime recTime;
}