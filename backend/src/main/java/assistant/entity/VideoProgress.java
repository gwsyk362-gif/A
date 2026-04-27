package assistant.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("video_progress")
public class VideoProgress {
    @TableId(value = "prog_id", type = IdType.AUTO)
    private Long progId;

    private Integer progUserId;
    private Integer progVidId;
    private Integer progLastPosition;
    private Integer progIsFinished;
    private LocalDateTime progUpdateTime;
}