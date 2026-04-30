package assistant.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("favorites_videos")
public class FavoriteVideo {
    @TableId(value = "fav_id", type = IdType.AUTO)
    private Integer favId;

    private Integer favUserId;    // 对应 fv_user_id
    private Integer favVidsId;     // 对应 fv_vid_id
    private LocalDateTime favCreateTime;
}