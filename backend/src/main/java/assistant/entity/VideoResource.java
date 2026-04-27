package assistant.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("video_resources")
public class VideoResource {
    @TableId(value = "vid_id", type = IdType.AUTO)
    private Integer vidId;

    private String vidTitle;
    private String vidUrl;
    private Integer vidSubId;
    private String vidCoverUrl;
    private Integer vidDuration;
    private String vidDescription;
    private LocalDateTime vidCreateTime;
}