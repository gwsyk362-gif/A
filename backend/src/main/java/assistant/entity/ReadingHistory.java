package assistant.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("reading_history")
public class ReadingHistory {
    @TableId(value = "history_id", type = IdType.AUTO)
    private Long historyId;

    private Integer userId;
    private Integer articleId;
    private LocalDateTime viewTime;
}