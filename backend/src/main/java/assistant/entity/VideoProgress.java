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

    private Integer progUserId;      // 用户ID (关联 users.user_id)
    private Integer progVidId;       // 视频ID (关联 video_resources.vid_id)
    private Integer progLastPosition; // 上次播放到的时间点(秒)
    private Integer progIsFinished;  // 是否看完(0:未完, 1:已完)
    private LocalDateTime progUpdateTime; // 最后观看时间
}