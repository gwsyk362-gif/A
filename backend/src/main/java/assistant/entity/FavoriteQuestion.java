package assistant.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("favorites_questions")
public class FavoriteQuestion {
    @TableId(value = "fav_id", type = IdType.AUTO)
    private Integer favId;

    private Integer favUserId;   // 对应 fav_user_id
    private Integer favQuesId;   // 对应 fav_ques_id
    private LocalDateTime favCreateTime;
}