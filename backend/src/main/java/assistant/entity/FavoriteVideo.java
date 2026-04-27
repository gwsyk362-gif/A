package assistant.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("favorite_videos")
public class FavoriteVideo {
    @TableId(value = "fv_id", type = IdType.AUTO)
    private Integer fvId;

    private Integer fvUserId;    // 对应 fv_user_id
    private Integer fvVidId;     // 对应 fv_vid_id
    private LocalDateTime fvCreateTime;
}