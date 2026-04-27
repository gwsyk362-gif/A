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

    private String vidTitle;       // 视频标题
    private String vidUrl;         // 视频存储地址或第三方链接
    private Integer vidSubId;      // 所属科目ID
    private String vidCoverUrl;    // 封面图链接
    private Integer vidDuration;   // 视频总时长(秒)
    private String vidDescription; // 视频简述
    private LocalDateTime vidCreateTime; // 上传时间
}